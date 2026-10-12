package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.common.BusinessException;
import com.warehouse.entity.BaseMaterial;
import com.warehouse.entity.BaseWarehouse;
import com.warehouse.entity.BizStock;
import com.warehouse.entity.BizStockFlow;
import com.warehouse.entity.BizStockWarning;
import com.warehouse.mapper.BaseMaterialMapper;
import com.warehouse.mapper.BaseWarehouseMapper;
import com.warehouse.mapper.BizStockFlowMapper;
import com.warehouse.mapper.BizStockWarningMapper;
import com.warehouse.service.BizStockService;
import com.warehouse.service.BizStockWarningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 库存预警业务实现。
 *
 * 包含三类预警：
 * 1 低于安全库存 —— 现有库存不足以覆盖补货提前期内的消耗；
 * 2 呆滞物料   —— 有库存但连续 90 天没有出库记录，占用仓储与资金；
 * 3 临近保质期 —— 批次有效期在 90 天内，需要优先出库。
 */
@Service
@RequiredArgsConstructor
public class BizStockWarningServiceImpl extends ServiceImpl<BizStockWarningMapper, BizStockWarning>
        implements BizStockWarningService {

    /** 呆滞判定天数 */
    private static final int SLOW_MOVING_DAYS = 90;
    /** 临期判定天数 */
    private static final int NEAR_EXPIRE_DAYS = 90;

    private final BizStockService stockService;
    private final BaseMaterialMapper materialMapper;
    private final BaseWarehouseMapper warehouseMapper;
    private final BizStockFlowMapper flowMapper;

    @Override
    public IPage<BizStockWarning> pageQuery(long pageNum, long pageSize, Integer warningType, Integer handled) {
        LambdaQueryWrapper<BizStockWarning> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warningType != null, BizStockWarning::getWarningType, warningType)
                .eq(handled != null, BizStockWarning::getHandled, handled)
                .orderByAsc(BizStockWarning::getWarningLevel)
                .orderByDesc(BizStockWarning::getId);
        IPage<BizStockWarning> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        fillNames(page.getRecords());
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateWarnings() {
        List<BizStock> stocks = stockService.list();
        Map<Long, BaseMaterial> materialMap = materialMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseMaterial::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> warehouseNames = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getWarehouseName, (a, b) -> a));

        // 近 90 天内发生过出库的物料，用于判断呆滞
        LocalDateTime start = LocalDateTime.now().minusDays(SLOW_MOVING_DAYS);
        Set<Long> activeMaterialIds = flowMapper.selectList(new LambdaQueryWrapper<BizStockFlow>()
                        .eq(BizStockFlow::getFlowType, 2)
                        .ge(BizStockFlow::getCreateTime, start))
                .stream().map(BizStockFlow::getMaterialId).collect(Collectors.toSet());

        // 每次扫描前清空未处理的旧预警，保证预警列表始终反映最新库存状态
        this.remove(new LambdaQueryWrapper<BizStockWarning>().eq(BizStockWarning::getHandled, 0));

        int count = 0;
        LocalDate today = LocalDate.now();
        for (BizStock stock : stocks) {
            BaseMaterial material = materialMap.get(stock.getMaterialId());
            if (material == null) {
                continue;
            }
            String warehouseName = warehouseNames.getOrDefault(stock.getWarehouseId(), "未知仓库");
            BigDecimal currentQty = nvl(stock.getQuantity());
            BigDecimal safetyStock = nvl(material.getSafetyStock());

            // 预警一：低于安全库存
            if (safetyStock.compareTo(BigDecimal.ZERO) > 0 && currentQty.compareTo(safetyStock) <= 0) {
                BigDecimal half = safetyStock.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                int level = currentQty.compareTo(half) <= 0 ? 1 : 2;
                saveWarning(stock, 1, level, currentQty, safetyStock,
                        warehouseName + "的【" + material.getMaterialName() + "】当前库存 "
                                + strip(currentQty) + "，已低于安全库存 " + strip(safetyStock));
                count++;
            }

            // 预警二：呆滞物料
            if (currentQty.compareTo(BigDecimal.ZERO) > 0 && !activeMaterialIds.contains(stock.getMaterialId())) {
                saveWarning(stock, 2, 3, currentQty, BigDecimal.ZERO,
                        "【" + material.getMaterialName() + "】已有 " + SLOW_MOVING_DAYS
                                + " 天未发生出库，存在呆滞积压风险");
                count++;
            }

            // 预警三：临近保质期
            if (stock.getExpireDate() != null
                    && !stock.getExpireDate().isAfter(today.plusDays(NEAR_EXPIRE_DAYS))) {
                long remainDays = ChronoUnit.DAYS.between(today, stock.getExpireDate());
                int level = remainDays <= 30 ? 1 : 2;
                saveWarning(stock, 3, level, currentQty, BigDecimal.ZERO,
                        "批次 " + stock.getBatchNo() + " 的【" + material.getMaterialName()
                                + "】将于 " + stock.getExpireDate() + " 到期，剩余 " + remainDays + " 天");
                count++;
            }
        }
        return count;
    }

    @Override
    public void handle(Long id, String handleRemark) {
        BizStockWarning warning = this.getById(id);
        if (warning == null) {
            throw new BusinessException("预警记录不存在");
        }
        BizStockWarning update = new BizStockWarning();
        update.setId(id);
        update.setHandled(1);
        update.setHandleRemark(StringUtils.hasText(handleRemark) ? handleRemark : "已处理");
        this.updateById(update);
    }

    private void saveWarning(BizStock stock, int type, int level, BigDecimal currentQty,
                             BigDecimal threshold, String msg) {
        BizStockWarning warning = new BizStockWarning();
        warning.setWarehouseId(stock.getWarehouseId());
        warning.setMaterialId(stock.getMaterialId());
        warning.setWarningType(type);
        warning.setWarningLevel(level);
        warning.setCurrentQty(currentQty);
        warning.setThresholdValue(threshold);
        warning.setWarningMsg(msg);
        warning.setHandled(0);
        this.save(warning);
    }

    private void fillNames(List<BizStockWarning> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Map<Long, BaseMaterial> materials = materialMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseMaterial::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> warehouses = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getWarehouseName, (a, b) -> a));
        records.forEach(record -> {
            BaseMaterial material = materials.get(record.getMaterialId());
            if (material != null) {
                record.setMaterialCode(material.getMaterialCode());
                record.setMaterialName(material.getMaterialName());
            }
            record.setWarehouseName(warehouses.get(record.getWarehouseId()));
        });
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String strip(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
