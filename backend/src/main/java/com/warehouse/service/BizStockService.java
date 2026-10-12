package com.warehouse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.BizStock;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 库存业务接口。
 * 库存的增减统一由本接口负责，保证「库存表 + 库存流水表」在同一事务内完成写入。
 */
public interface BizStockService extends IService<BizStock> {

    /** 分页查询库存 */
    IPage<BizStock> pageQuery(long pageNum, long pageSize, String keyword, Long warehouseId, Boolean onlyWarning);

    /** 查询某仓库某物料的可用库存数量 */
    BigDecimal getAvailableQty(Long warehouseId, Long materialId);

    /**
     * 增加库存并写入流水。
     *
     * @param avgCost  入库成本单价，用于计算移动加权平均成本
     * @param flowType 流水类型：1 入库，3 调拨入，5 盘盈
     */
    void increaseStock(Long warehouseId, Long materialId, String batchNo, BigDecimal qty,
                       BigDecimal avgCost, LocalDate expireDate, int flowType, String refNo, Long refId,
                       String operator, String remark);

    /**
     * 减少库存并写入流水，库存不足时抛出业务异常。
     *
     * @param flowType 流水类型：2 出库，4 调拨出，6 盘亏
     */
    void decreaseStock(Long warehouseId, Long materialId, String batchNo, BigDecimal qty,
                       int flowType, String refNo, Long refId, String operator, String remark);

    /** 库存金额分布（按仓库统计） */
    List<Map<String, Object>> stockAmountByWarehouse();

    /** ABC 分类分析：按物料统计库存金额并按累计占比划分 A/B/C */
    List<Map<String, Object>> abcAnalysis();

    /** 库存周转率分析 */
    List<Map<String, Object>> turnoverAnalysis(int days);

    /** 按物料维度汇总库存（跨仓库合并） */
    List<Map<String, Object>> summaryByMaterial();

    /** 按仓库维度汇总库存 */
    List<Map<String, Object>> summaryByWarehouse();
}
