package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.common.BusinessException;
import com.warehouse.entity.BaseMaterial;
import com.warehouse.entity.BaseWarehouse;
import com.warehouse.entity.BizStock;
import com.warehouse.entity.BizStockFlow;
import com.warehouse.mapper.BaseMaterialMapper;
import com.warehouse.mapper.BaseWarehouseMapper;
import com.warehouse.mapper.BizStockFlowMapper;
import com.warehouse.mapper.BizStockMapper;
import com.warehouse.service.BizStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 库存业务实现。
 *
 * 设计要点：
 * 1. 库存数量的增减统一收敛到 increaseStock / decreaseStock 两个方法，保证「库存表 + 流水表」同事务写入；
 * 2. 出库采用「条件更新」防止并发超卖，即仅当剩余数量充足时才执行扣减；
 * 3. 入库采用移动加权平均法更新成本单价。
 */
@Service
@RequiredArgsConstructor
public class BizStockServiceImpl extends ServiceImpl<BizStockMapper, BizStock> implements BizStockService {

    /** 默认批次号：不启用批次管理的物料统一使用该值 */
    private static final String DEFAULT_BATCH = "DEFAULT";

    private final BizStockFlowMapper flowMapper;
    private final BaseMaterialMapper materialMapper;
    private final BaseWarehouseMapper warehouseMapper;

    @Override
    public IPage<BizStock> pageQuery(long pageNum, long pageSize, String keyword, Long warehouseId, Boolean onlyWarning) {
        IPage<BizStock> page = baseMapper.selectStockPage(new Page<>(pageNum, pageSize), keyword, warehouseId, onlyWarning);
        page.getRecords().forEach(stock -> stock.setAmount(
                nvl(stock.getQuantity()).multiply(nvl(stock.getAvgCost())).setScale(2, RoundingMode.HALF_UP)));
        return page;
    }

    @Override
    public BigDecimal getAvailableQty(Long warehouseId, Long materialId) {
        return this.list(new LambdaQueryWrapper<BizStock>()
                        .eq(BizStock::getWarehouseId, warehouseId)
                        .eq(BizStock::getMaterialId, materialId))
                .stream()
                .map(stock -> nvl(stock.getQuantity()).subtract(nvl(stock.getLockedQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseStock(Long warehouseId, Long materialId, String batchNo, BigDecimal qty,
                              BigDecimal avgCost, LocalDate expireDate, int flowType, String refNo, Long refId,
                              String operator, String remark) {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("入库数量必须大于 0");
        }
        String batch = StringUtils.hasText(batchNo) ? batchNo : DEFAULT_BATCH;
        BizStock stock = this.getOne(new LambdaQueryWrapper<BizStock>()
                .eq(BizStock::getWarehouseId, warehouseId)
                .eq(BizStock::getMaterialId, materialId)
                .eq(BizStock::getBatchNo, batch)
                .last("limit 1"));

        BigDecimal before = stock == null ? BigDecimal.ZERO : nvl(stock.getQuantity());
        BigDecimal after = before.add(qty);

        if (stock == null) {
            stock = new BizStock();
            stock.setWarehouseId(warehouseId);
            stock.setMaterialId(materialId);
            stock.setBatchNo(batch);
            stock.setQuantity(after);
            stock.setLockedQty(BigDecimal.ZERO);
            stock.setAvgCost(nvl(avgCost));
            stock.setExpireDate(expireDate);
            stock.setLastInTime(LocalDateTime.now());
            this.save(stock);
        } else {
            // 移动加权平均成本 =（原有库存金额 + 本次入库金额）÷ 入库后总数量
            BigDecimal oldAmount = before.multiply(nvl(stock.getAvgCost()));
            BigDecimal inAmount = qty.multiply(nvl(avgCost));
            BigDecimal newAvgCost = after.compareTo(BigDecimal.ZERO) == 0
                    ? nvl(avgCost)
                    : oldAmount.add(inAmount).divide(after, 4, RoundingMode.HALF_UP);
            stock.setQuantity(after);
            stock.setAvgCost(newAvgCost);
            if (expireDate != null) {
                stock.setExpireDate(expireDate);
            }
            stock.setLastInTime(LocalDateTime.now());
            this.updateById(stock);
        }

        writeFlow(warehouseId, materialId, flowType, qty, before, after, batch, refNo, refId, operator, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decreaseStock(Long warehouseId, Long materialId, String batchNo, BigDecimal qty,
                              int flowType, String refNo, Long refId, String operator, String remark) {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("出库数量必须大于 0");
        }
        String batch = StringUtils.hasText(batchNo) ? batchNo : DEFAULT_BATCH;
        BizStock stock = this.getOne(new LambdaQueryWrapper<BizStock>()
                .eq(BizStock::getWarehouseId, warehouseId)
                .eq(BizStock::getMaterialId, materialId)
                .eq(BizStock::getBatchNo, batch)
                .last("limit 1"));

        BigDecimal before = stock == null ? BigDecimal.ZERO : nvl(stock.getQuantity());
        if (stock == null || before.compareTo(qty) < 0) {
            BaseMaterial material = materialMapper.selectById(materialId);
            String materialName = material == null ? ("物料#" + materialId) : material.getMaterialName();
            throw new BusinessException("【" + materialName + "】当前库存 " + before.stripTrailingZeros().toPlainString()
                    + "，不足本次出库数量 " + qty.stripTrailingZeros().toPlainString());
        }

        // 条件更新：只有库存仍然充足时才扣减，避免并发场景下的超卖与负库存
        boolean updated = this.update(new LambdaUpdateWrapper<BizStock>()
                .eq(BizStock::getId, stock.getId())
                .ge(BizStock::getQuantity, qty)
                .setSql("quantity = quantity - " + qty.toPlainString())
                .set(BizStock::getLastOutTime, LocalDateTime.now()));
        if (!updated) {
            throw new BusinessException("库存扣减失败，数据可能已被其他单据更新，请刷新后重试");
        }

        // 重新读取，确保流水中记录的结存数量准确
        BizStock latest = this.getById(stock.getId());
        BigDecimal after = latest == null ? before.subtract(qty) : nvl(latest.getQuantity());
        writeFlow(warehouseId, materialId, flowType, qty.negate(), before, after, batch, refNo, refId, operator, remark);
    }

    @Override
    public List<Map<String, Object>> stockAmountByWarehouse() {
        List<BizStock> stocks = this.list();
        Map<Long, BigDecimal> amountMap = new LinkedHashMap<>();
        stocks.forEach(stock -> amountMap.merge(stock.getWarehouseId(),
                nvl(stock.getQuantity()).multiply(nvl(stock.getAvgCost())), BigDecimal::add));

        Map<Long, String> warehouseNames = nameMap();
        List<Map<String, Object>> result = new ArrayList<>();
        amountMap.forEach((warehouseId, amount) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("warehouseId", warehouseId);
            row.put("warehouseName", warehouseNames.getOrDefault(warehouseId, "未知仓库"));
            row.put("amount", amount.setScale(2, RoundingMode.HALF_UP));
            result.add(row);
        });
        return result;
    }

    @Override
    public List<Map<String, Object>> abcAnalysis() {
        List<BizStock> stocks = this.list();
        List<BaseMaterial> materials = materialMapper.selectList(null);
        Map<Long, BaseMaterial> materialMap = materials.stream()
                .collect(Collectors.toMap(BaseMaterial::getId, Function.identity(), (a, b) -> a));

        // 第一步：按物料汇总出库前的库存金额
        Map<Long, BigDecimal> amountMap = new HashMap<>();
        stocks.forEach(stock -> amountMap.merge(stock.getMaterialId(),
                nvl(stock.getQuantity()).multiply(nvl(stock.getAvgCost())), BigDecimal::add));

        BigDecimal total = amountMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return new ArrayList<>();
        }

        // 第二步：按金额降序排列，计算累计占比（帕累托法则）
        List<Map<String, Object>> rows = amountMap.entrySet().stream()
                .map(entry -> {
                    BaseMaterial material = materialMap.get(entry.getKey());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("materialId", entry.getKey());
                    row.put("materialCode", material == null ? "" : material.getMaterialCode());
                    row.put("materialName", material == null ? "" : material.getMaterialName());
                    row.put("amount", entry.getValue().setScale(2, RoundingMode.HALF_UP));
                    row.put("_amount", entry.getValue());
                    return row;
                })
                .sorted(Comparator.comparing((Map<String, Object> row) -> (BigDecimal) row.get("_amount"),
                        Comparator.reverseOrder()))
                .collect(Collectors.toList());

        // 第三步：累计占比 ≤70% 为 A 类，≤90% 为 B 类，其余为 C 类
        BigDecimal cumulative = BigDecimal.ZERO;
        for (Map<String, Object> row : rows) {
            BigDecimal amount = (BigDecimal) row.get("_amount");
            cumulative = cumulative.add(amount);
            BigDecimal ratio = cumulative.multiply(BigDecimal.valueOf(100))
                    .divide(total, 2, RoundingMode.HALF_UP);
            String abcType = ratio.compareTo(BigDecimal.valueOf(70)) <= 0 ? "A"
                    : ratio.compareTo(BigDecimal.valueOf(90)) <= 0 ? "B" : "C";
            row.put("cumulativeRatio", ratio);
            row.put("abcType", abcType);
            row.put("amountRatio", amount.multiply(BigDecimal.valueOf(100))
                    .divide(total, 2, RoundingMode.HALF_UP));
            row.remove("_amount");
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> turnoverAnalysis(int days) {
        LocalDateTime start = LocalDateTime.now().minusDays(days);
        List<BizStockFlow> flows = flowMapper.selectList(new LambdaQueryWrapper<BizStockFlow>()
                .eq(BizStockFlow::getFlowType, 2)
                .ge(BizStockFlow::getCreateTime, start));

        Map<Long, BigDecimal> outQtyMap = new HashMap<>();
        flows.forEach(flow -> outQtyMap.merge(flow.getMaterialId(), nvl(flow.getChangeQty()).abs(), BigDecimal::add));

        List<BizStock> stocks = this.list();
        Map<Long, BigDecimal> stockQtyMap = new HashMap<>();
        stocks.forEach(stock -> stockQtyMap.merge(stock.getMaterialId(), nvl(stock.getQuantity()), BigDecimal::add));

        List<BaseMaterial> materials = materialMapper.selectList(null);
        Map<Long, BaseMaterial> materialMap = materials.stream()
                .collect(Collectors.toMap(BaseMaterial::getId, Function.identity(), (a, b) -> a));

        List<Map<String, Object>> result = new ArrayList<>();
        stockQtyMap.keySet().stream().sorted().forEach(materialId -> {
            BigDecimal outQty = outQtyMap.getOrDefault(materialId, BigDecimal.ZERO);
            BigDecimal avgStock = stockQtyMap.get(materialId);
            BigDecimal turnover = avgStock.compareTo(BigDecimal.ZERO) <= 0
                    ? BigDecimal.ZERO
                    : outQty.divide(avgStock, 2, RoundingMode.HALF_UP);
            BaseMaterial material = materialMap.get(materialId);

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("materialId", materialId);
            row.put("materialCode", material == null ? "" : material.getMaterialCode());
            row.put("materialName", material == null ? "" : material.getMaterialName());
            row.put("currentQty", avgStock.setScale(2, RoundingMode.HALF_UP));
            row.put("outQty", outQty.setScale(2, RoundingMode.HALF_UP));
            row.put("turnoverRate", turnover);
            // 统计周期内没有出库记录的物料视为呆滞物料
            row.put("slowMoving", outQty.compareTo(BigDecimal.ZERO) == 0);
            result.add(row);
        });
        return result;
    }

    /** 写入库存流水 */
    private void writeFlow(Long warehouseId, Long materialId, int flowType, BigDecimal changeQty,
                           BigDecimal beforeQty, BigDecimal afterQty, String batchNo,
                           String refNo, Long refId, String operator, String remark) {
        BizStockFlow flow = new BizStockFlow();
        flow.setFlowNo("FL" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + ThreadLocalRandom.current().nextInt(100, 999));
        flow.setWarehouseId(warehouseId);
        flow.setMaterialId(materialId);
        flow.setFlowType(flowType);
        flow.setChangeQty(changeQty);
        flow.setBeforeQty(beforeQty);
        flow.setAfterQty(afterQty);
        flow.setBatchNo(batchNo);
        flow.setRefNo(refNo);
        flow.setRefId(refId);
        flow.setOperator(operator);
        flow.setRemark(remark);
        flowMapper.insert(flow);
    }

    /** 仓库主键与名称映射 */
    private Map<Long, String> nameMap() {
        return warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getWarehouseName, (a, b) -> a));
    }

    @Override
    public List<Map<String, Object>> summaryByMaterial() {
        return baseMapper.selectSummaryByMaterial();
    }

    @Override
    public List<Map<String, Object>> summaryByWarehouse() {
        return baseMapper.selectSummaryByWarehouse();
    }

    /** BigDecimal 空值保护 */
    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
