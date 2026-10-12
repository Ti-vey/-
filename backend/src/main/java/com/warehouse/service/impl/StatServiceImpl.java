package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.warehouse.entity.BaseMaterial;
import com.warehouse.entity.BaseWarehouse;
import com.warehouse.entity.BizStock;
import com.warehouse.entity.BizInbound;
import com.warehouse.entity.BizOutbound;
import com.warehouse.entity.BizReplenishment;
import com.warehouse.entity.BizStock;
import com.warehouse.entity.BizStockFlow;
import com.warehouse.entity.BizStockWarning;
import com.warehouse.entity.BizTransfer;
import com.warehouse.mapper.BaseMaterialMapper;
import com.warehouse.mapper.BaseWarehouseMapper;
import com.warehouse.mapper.BizInboundMapper;
import com.warehouse.mapper.BizOutboundMapper;
import com.warehouse.mapper.BizReplenishmentMapper;
import com.warehouse.mapper.BizStockFlowMapper;
import com.warehouse.mapper.BizStockMapper;
import com.warehouse.mapper.BizStockWarningMapper;
import com.warehouse.mapper.BizTransferMapper;
import com.warehouse.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 统计分析业务实现：为首页看板与报表页面提供聚合数据。
 * 统计逻辑尽量在 Java 侧完成，避免依赖特定数据库版本的窗口函数，便于部署。
 */
@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private static final int NEAR_EXPIRE_DAYS = 90;

    private final BaseMaterialMapper materialMapper;
    private final BaseWarehouseMapper warehouseMapper;
    private final BizStockMapper stockMapper;
    private final BizStockFlowMapper flowMapper;
    private final BizInboundMapper inboundMapper;
    private final BizOutboundMapper outboundMapper;
    private final BizTransferMapper transferMapper;
    private final BizStockWarningMapper warningMapper;
    private final BizReplenishmentMapper replenishmentMapper;

    @Override
    public Map<String, Object> dashboard() {
        List<BaseMaterial> materials = materialMapper.selectList(null);
        Map<Long, BaseMaterial> materialMap = materials.stream()
                .collect(Collectors.toMap(BaseMaterial::getId, Function.identity(), (a, b) -> a));
        List<BizStock> stocks = stockMapper.selectList(null);

        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        int lowStockCount = 0;
        int nearExpireCount = 0;
        LocalDate today = LocalDate.now();

        for (BizStock stock : stocks) {
            BigDecimal qty = nvl(stock.getQuantity());
            totalQty = totalQty.add(qty);
            totalAmount = totalAmount.add(qty.multiply(nvl(stock.getAvgCost())));

            BaseMaterial material = materialMap.get(stock.getMaterialId());
            if (material != null && nvl(material.getSafetyStock()).compareTo(BigDecimal.ZERO) > 0
                    && qty.compareTo(material.getSafetyStock()) <= 0) {
                lowStockCount++;
            }
            if (stock.getExpireDate() != null && !stock.getExpireDate().isAfter(today.plusDays(NEAR_EXPIRE_DAYS))) {
                nearExpireCount++;
            }
        }

        LocalDateTime todayStart = today.atStartOfDay();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("materialCount", materials.size());
        data.put("warehouseCount", warehouseMapper.selectCount(null));
        data.put("stockTotalQty", totalQty.setScale(2, RoundingMode.HALF_UP));
        data.put("stockTotalAmount", totalAmount.setScale(2, RoundingMode.HALF_UP));
        data.put("lowStockCount", lowStockCount);
        data.put("nearExpireCount", nearExpireCount);
        data.put("unhandledWarningCount", warningMapper.selectCount(
                new LambdaQueryWrapper<BizStockWarning>().eq(BizStockWarning::getHandled, 0)));
        data.put("pendingReplenishCount", replenishmentMapper.selectCount(
                new LambdaQueryWrapper<BizReplenishment>().eq(BizReplenishment::getStatus, 0)));
        data.put("pendingInboundCount", inboundMapper.selectCount(
                new LambdaQueryWrapper<BizInbound>().eq(BizInbound::getStatus, 1)));
        data.put("pendingOutboundCount", outboundMapper.selectCount(
                new LambdaQueryWrapper<BizOutbound>().eq(BizOutbound::getStatus, 1)));
        data.put("pendingTransferCount", transferMapper.selectCount(
                new LambdaQueryWrapper<BizTransfer>().eq(BizTransfer::getStatus, 1)));
        data.put("todayInboundCount", inboundMapper.selectCount(
                new LambdaQueryWrapper<BizInbound>().ge(BizInbound::getCreateTime, todayStart)));
        data.put("todayOutboundCount", outboundMapper.selectCount(
                new LambdaQueryWrapper<BizOutbound>().ge(BizOutbound::getCreateTime, todayStart)));
        data.put("abcDistribution", abcDistribution(materials, stocks));
        return data;
    }

    @Override
    public List<Map<String, Object>> inoutTrend(int days) {
        int span = days <= 0 ? 7 : days;
        LocalDate startDate = LocalDate.now().minusDays(span - 1L);
        List<BizStockFlow> flows = flowMapper.selectList(new LambdaQueryWrapper<BizStockFlow>()
                .ge(BizStockFlow::getCreateTime, startDate.atStartOfDay()));

        Map<String, BigDecimal> inboundMap = new HashMap<>();
        Map<String, BigDecimal> outboundMap = new HashMap<>();
        for (BizStockFlow flow : flows) {
            if (flow.getCreateTime() == null) {
                continue;
            }
            String date = flow.getCreateTime().toLocalDate().toString();
            BigDecimal changeQty = nvl(flow.getChangeQty());
            if (changeQty.signum() >= 0) {
                inboundMap.merge(date, changeQty, BigDecimal::add);
            } else {
                outboundMap.merge(date, changeQty.abs(), BigDecimal::add);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < span; i++) {
            String date = startDate.plusDays(i).toString();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", date);
            row.put("inboundQty", inboundMap.getOrDefault(date, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
            row.put("outboundQty", outboundMap.getOrDefault(date, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
            result.add(row);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> topMaterials(int limit) {
        Map<Long, BaseMaterial> materialMap = materialMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseMaterial::getId, Function.identity(), (a, b) -> a));
        Map<Long, BigDecimal> amountMap = new HashMap<>();
        stockMapper.selectList(null).forEach(stock -> amountMap.merge(stock.getMaterialId(),
                nvl(stock.getQuantity()).multiply(nvl(stock.getAvgCost())), BigDecimal::add));

        int top = limit <= 0 ? 10 : limit;
        List<Map<String, Object>> rows = amountMap.entrySet().stream()
                .map(entry -> {
                    BaseMaterial material = materialMap.get(entry.getKey());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("materialId", entry.getKey());
                    row.put("materialCode", material == null ? "" : material.getMaterialCode());
                    row.put("materialName", material == null ? "" : material.getMaterialName());
                    row.put("amount", entry.getValue().setScale(2, RoundingMode.HALF_UP));
                    return row;
                })
                .sorted(Comparator.comparing((Map<String, Object> row) -> (BigDecimal) row.get("amount"),
                        Comparator.reverseOrder()))
                .limit(top)
                .collect(Collectors.toList());
        return rows;
    }

    @Override
    public List<Map<String, Object>> lowStockList(int limit) {
        Map<Long, BaseMaterial> materialMap = materialMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseMaterial::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> warehouseNames = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getWarehouseName, (a, b) -> a));

        int top = limit <= 0 ? 5 : limit;
        return stockMapper.selectList(null).stream()
                .filter(stock -> {
                    BaseMaterial material = materialMap.get(stock.getMaterialId());
                    return material != null && nvl(material.getSafetyStock()).compareTo(BigDecimal.ZERO) > 0
                            && nvl(stock.getQuantity()).compareTo(material.getSafetyStock()) <= 0;
                })
                // 按缺口比例降序，缺口越大越靠前，便于优先处理最紧急的物料
                .sorted(Comparator.comparing((BizStock stock) -> {
                    BaseMaterial material = materialMap.get(stock.getMaterialId());
                    BigDecimal safety = nvl(material.getSafetyStock());
                    return safety.subtract(nvl(stock.getQuantity()))
                            .divide(safety, 4, RoundingMode.HALF_UP);
                }).reversed())
                .limit(top)
                .map(stock -> {
                    BaseMaterial material = materialMap.get(stock.getMaterialId());
                    BigDecimal safety = nvl(material.getSafetyStock());
                    BigDecimal quantity = nvl(stock.getQuantity());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("materialId", stock.getMaterialId());
                    row.put("materialCode", material.getMaterialCode());
                    row.put("materialName", material.getMaterialName());
                    row.put("warehouseName", warehouseNames.getOrDefault(stock.getWarehouseId(), "未知仓库"));
                    row.put("quantity", quantity.setScale(2, RoundingMode.HALF_UP));
                    row.put("safetyStock", safety.setScale(2, RoundingMode.HALF_UP));
                    row.put("gap", safety.subtract(quantity).setScale(2, RoundingMode.HALF_UP));
                    return row;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> expiringList(int limit) {
        Map<Long, BaseMaterial> materialMap = materialMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseMaterial::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> warehouseNames = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getWarehouseName, (a, b) -> a));

        LocalDate today = LocalDate.now();
        int top = limit <= 0 ? 5 : limit;
        return stockMapper.selectList(null).stream()
                .filter(stock -> stock.getExpireDate() != null
                        && !stock.getExpireDate().isAfter(today.plusDays(NEAR_EXPIRE_DAYS)))
                .sorted(Comparator.comparing(BizStock::getExpireDate))
                .limit(top)
                .map(stock -> {
                    BaseMaterial material = materialMap.get(stock.getMaterialId());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("materialId", stock.getMaterialId());
                    row.put("materialCode", material == null ? "" : material.getMaterialCode());
                    row.put("materialName", material == null ? "" : material.getMaterialName());
                    row.put("warehouseName", warehouseNames.getOrDefault(stock.getWarehouseId(), "未知仓库"));
                    row.put("batchNo", stock.getBatchNo());
                    row.put("quantity", nvl(stock.getQuantity()).setScale(2, RoundingMode.HALF_UP));
                    row.put("expireDate", stock.getExpireDate().toString());
                    row.put("remainDays", java.time.temporal.ChronoUnit.DAYS.between(today, stock.getExpireDate()));
                    return row;
                })
                .collect(Collectors.toList());
    }

    /** 按物料档案中登记的 ABC 分类统计物料数量与库存金额占比 */
    private List<Map<String, Object>> abcDistribution(List<BaseMaterial> materials, List<BizStock> stocks) {
        Map<Long, BaseMaterial> materialMap = materials.stream()
                .collect(Collectors.toMap(BaseMaterial::getId, Function.identity(), (a, b) -> a));
        Map<String, BigDecimal> amountByType = new LinkedHashMap<>();
        amountByType.put("A", BigDecimal.ZERO);
        amountByType.put("B", BigDecimal.ZERO);
        amountByType.put("C", BigDecimal.ZERO);

        for (BizStock stock : stocks) {
            BaseMaterial material = materialMap.get(stock.getMaterialId());
            if (material == null) {
                continue;
            }
            String type = material.getAbcType() == null ? "C" : material.getAbcType();
            amountByType.merge(type, nvl(stock.getQuantity()).multiply(nvl(stock.getAvgCost())), BigDecimal::add);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        amountByType.forEach((type, amount) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("abcType", type);
            row.put("amount", amount.setScale(2, RoundingMode.HALF_UP));
            row.put("materialCount", materials.stream()
                    .filter(m -> type.equals(m.getAbcType() == null ? "C" : m.getAbcType())).count());
            result.add(row);
        });
        return result;
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
