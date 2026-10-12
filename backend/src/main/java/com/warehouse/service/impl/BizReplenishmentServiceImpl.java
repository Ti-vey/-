package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.common.BusinessException;
import com.warehouse.entity.BaseMaterial;
import com.warehouse.entity.BaseUnit;
import com.warehouse.entity.BaseWarehouse;
import com.warehouse.entity.BizReplenishment;
import com.warehouse.entity.BizStock;
import com.warehouse.entity.BizStockFlow;
import com.warehouse.mapper.BaseMaterialMapper;
import com.warehouse.mapper.BaseUnitMapper;
import com.warehouse.mapper.BaseWarehouseMapper;
import com.warehouse.mapper.BizReplenishmentMapper;
import com.warehouse.mapper.BizStockFlowMapper;
import com.warehouse.service.BizReplenishmentService;
import com.warehouse.service.BizStockService;
import com.warehouse.util.OrderNoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 补货计划业务实现 —— 本系统的核心创新点。
 *
 * 计算过程：
 * 1. 取近 30 天的出库流水，算出每个物料的平均日消耗量；
 * 2. 安全库存取「物料档案配置值」与「平均日耗 × 补货提前期」的较大者，兼顾经验值与统计数据；
 * 3. 当现有库存不高于安全库存时触发补货建议；
 * 4. 建议补货量在「补足到两倍安全库存的缺口」与「经济订货批量 EOQ」之间取较大值，
 *    EOQ = √(2 × 年需求量 × 每次订货成本 ÷ 单位年持有成本)，避免单次订货过少导致频繁下单。
 */
@Service
@RequiredArgsConstructor
public class BizReplenishmentServiceImpl extends ServiceImpl<BizReplenishmentMapper, BizReplenishment>
        implements BizReplenishmentService {

    /** 统计周期（天） */
    private static final int STAT_DAYS = 30;
    /** 默认补货提前期（天） */
    private static final int DEFAULT_LEAD_TIME = 3;
    /** 每次订货成本（元），实际项目中可放入系统参数表配置 */
    private static final BigDecimal ORDER_COST = BigDecimal.valueOf(50);
    /** 年持有成本率：按采购单价的 20% 估算 */
    private static final BigDecimal HOLDING_RATE = BigDecimal.valueOf(0.2);

    private final BizStockService stockService;
    private final BaseMaterialMapper materialMapper;
    private final BaseUnitMapper unitMapper;
    private final BaseWarehouseMapper warehouseMapper;
    private final BizStockFlowMapper flowMapper;

    @Override
    public IPage<BizReplenishment> pageQuery(long pageNum, long pageSize, Integer status, Integer urgency) {
        LambdaQueryWrapper<BizReplenishment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, BizReplenishment::getStatus, status)
                .eq(urgency != null, BizReplenishment::getUrgency, urgency)
                .orderByAsc(BizReplenishment::getUrgency)
                .orderByDesc(BizReplenishment::getId);
        IPage<BizReplenishment> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        fillNames(page.getRecords());
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BizReplenishment> generate() {
        List<BizStock> stocks = stockService.list();
        if (stocks.isEmpty()) {
            throw new BusinessException("暂无库存数据，无法生成补货建议");
        }
        Map<Long, BaseMaterial> materialMap = materialMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseMaterial::getId, Function.identity(), (a, b) -> a));

        // 近 30 天出库量统计
        LocalDateTime start = LocalDateTime.now().minusDays(STAT_DAYS);
        List<BizStockFlow> flows = flowMapper.selectList(new LambdaQueryWrapper<BizStockFlow>()
                .eq(BizStockFlow::getFlowType, 2)
                .ge(BizStockFlow::getCreateTime, start));
        Map<Long, BigDecimal> outQtyMap = new HashMap<>();
        flows.forEach(flow -> outQtyMap.merge(flow.getMaterialId(),
                nvl(flow.getChangeQty()).abs(), BigDecimal::add));

        // 重新生成前先清空历史「待处理」建议，避免重复堆积
        this.remove(new LambdaQueryWrapper<BizReplenishment>().eq(BizReplenishment::getStatus, 0));

        List<BizReplenishment> result = new ArrayList<>();
        for (BizStock stock : stocks) {
            BaseMaterial material = materialMap.get(stock.getMaterialId());
            if (material == null) {
                continue;
            }

            BigDecimal avgDailyUsage = outQtyMap.getOrDefault(stock.getMaterialId(), BigDecimal.ZERO)
                    .divide(BigDecimal.valueOf(STAT_DAYS), 4, RoundingMode.HALF_UP);
            BigDecimal configuredSafety = nvl(material.getSafetyStock());
            BigDecimal modelSafety = avgDailyUsage.multiply(BigDecimal.valueOf(DEFAULT_LEAD_TIME))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal safetyStock = configuredSafety.max(modelSafety);
            if (safetyStock.compareTo(BigDecimal.ZERO) <= 0) {
                // 既没有配置安全库存也没有消耗记录，说明该物料暂不需要管控
                continue;
            }

            BigDecimal currentQty = nvl(stock.getQuantity());
            if (currentQty.compareTo(safetyStock) > 0) {
                continue;
            }

            BigDecimal targetQty = safetyStock.multiply(BigDecimal.valueOf(2));
            BigDecimal gap = targetQty.subtract(currentQty);
            BigDecimal eoq = calcEoq(avgDailyUsage, material.getPurchasePrice());
            BigDecimal suggestQty = gap.max(eoq).setScale(2, RoundingMode.HALF_UP);
            if (suggestQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // 紧急程度：低于安全库存一半为紧急
            int urgency;
            if (currentQty.compareTo(safetyStock.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)) <= 0) {
                urgency = 1;
            } else if (currentQty.compareTo(safetyStock) <= 0) {
                urgency = 2;
            } else {
                urgency = 3;
            }

            BizReplenishment entity = new BizReplenishment();
            entity.setReplenishNo(OrderNoUtil.generate("RP"));
            entity.setMaterialId(stock.getMaterialId());
            entity.setWarehouseId(stock.getWarehouseId());
            entity.setCurrentQty(currentQty);
            entity.setSafetyStock(safetyStock);
            entity.setSuggestQty(suggestQty);
            entity.setEoqQty(eoq);
            entity.setAvgDailyUsage(avgDailyUsage);
            entity.setLeadTime(DEFAULT_LEAD_TIME);
            entity.setUrgency(urgency);
            entity.setStatus(0);
            entity.setRemark("系统依据近 " + STAT_DAYS + " 天消耗数据自动生成");
            this.save(entity);
            result.add(entity);
        }
        fillNames(result);
        return result;
    }

    @Override
    public void changeStatus(Long id, Integer status, String remark) {
        BizReplenishment entity = this.getById(id);
        if (entity == null) {
            throw new BusinessException("补货计划不存在");
        }
        BizReplenishment update = new BizReplenishment();
        update.setId(id);
        update.setStatus(status);
        if (StringUtils.hasText(remark)) {
            update.setRemark(remark);
        }
        this.updateById(update);
    }

    /** 计算经济订货批量 EOQ = √(2 × 年需求量 × 每次订货成本 ÷ 单位年持有成本) */
    private BigDecimal calcEoq(BigDecimal avgDailyUsage, BigDecimal purchasePrice) {
        BigDecimal annualDemand = avgDailyUsage.multiply(BigDecimal.valueOf(365));
        BigDecimal unitPrice = nvl(purchasePrice);
        BigDecimal holdingCost = unitPrice.multiply(HOLDING_RATE);
        if (annualDemand.compareTo(BigDecimal.ZERO) <= 0 || holdingCost.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        double eoq = Math.sqrt(annualDemand.doubleValue() * 2 * ORDER_COST.doubleValue() / holdingCost.doubleValue());
        return BigDecimal.valueOf(eoq).setScale(2, RoundingMode.HALF_UP);
    }

    private void fillNames(List<BizReplenishment> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Map<Long, BaseMaterial> materials = materialMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseMaterial::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> units = unitMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseUnit::getId, BaseUnit::getUnitName, (a, b) -> a));
        Map<Long, String> warehouses = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getWarehouseName, (a, b) -> a));
        records.forEach(record -> {
            BaseMaterial material = materials.get(record.getMaterialId());
            if (material != null) {
                record.setMaterialCode(material.getMaterialCode());
                record.setMaterialName(material.getMaterialName());
                record.setUnitName(units.get(material.getUnitId()));
                record.setAbcType(material.getAbcType());
            }
            record.setWarehouseName(warehouses.get(record.getWarehouseId()));
        });
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
