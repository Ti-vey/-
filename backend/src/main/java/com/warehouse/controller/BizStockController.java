package com.warehouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.BizStock;
import com.warehouse.entity.BizStockFlow;
import com.warehouse.service.BizStockFlowService;
import com.warehouse.service.BizStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 库存查询控制器：实时库存、可用库存、库存流水与库存分析。
 */
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class BizStockController {

    private final BizStockService stockService;
    private final BizStockFlowService stockFlowService;

    /** 实时库存分页查询，onlyWarning=true 时只返回低于安全库存的记录 */
    @GetMapping("/page")
    public Result<PageResult<BizStock>> page(@RequestParam(defaultValue = "1") long pageNum,
                                             @RequestParam(defaultValue = "10") long pageSize,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Long warehouseId,
                                             @RequestParam(required = false) Boolean onlyWarning) {
        IPage<BizStock> page = stockService.pageQuery(pageNum, pageSize, keyword, warehouseId, onlyWarning);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 查询某仓库某物料的可用库存，供出库单编辑时提示 */
    @GetMapping("/available")
    public Result<BigDecimal> available(@RequestParam Long warehouseId, @RequestParam Long materialId) {
        return Result.success(stockService.getAvailableQty(warehouseId, materialId));
    }

    /** 库存流水分页查询 */
    @GetMapping("/flow/page")
    public Result<PageResult<BizStockFlow>> flowPage(@RequestParam(defaultValue = "1") long pageNum,
                                                     @RequestParam(defaultValue = "10") long pageSize,
                                                     @RequestParam(required = false) Long materialId,
                                                     @RequestParam(required = false) Long warehouseId,
                                                     @RequestParam(required = false) Integer flowType) {
        IPage<BizStockFlow> page = stockFlowService.pageQuery(pageNum, pageSize, materialId, warehouseId, flowType);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 库存金额分布（按仓库） */
    @GetMapping("/amount-by-warehouse")
    public Result<List<Map<String, Object>>> amountByWarehouse() {
        return Result.success(stockService.stockAmountByWarehouse());
    }

    /** ABC 分类分析 */
    @GetMapping("/abc")
    public Result<List<Map<String, Object>>> abc() {
        return Result.success(stockService.abcAnalysis());
    }

    /** 库存周转率与呆滞分析 */
    @GetMapping("/turnover")
    public Result<List<Map<String, Object>>> turnover(@RequestParam(defaultValue = "30") int days) {
        return Result.success(stockService.turnoverAnalysis(days));
    }

    /** 按物料维度汇总库存 */
    @GetMapping("/summary-by-material")
    public Result<List<Map<String, Object>>> summaryByMaterial() {
        return Result.success(stockService.summaryByMaterial());
    }

    /** 按仓库维度汇总库存 */
    @GetMapping("/summary-by-warehouse")
    public Result<List<Map<String, Object>>> summaryByWarehouse() {
        return Result.success(stockService.summaryByWarehouse());
    }
}
