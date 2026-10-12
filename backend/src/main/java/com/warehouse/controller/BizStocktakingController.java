package com.warehouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.BizStocktaking;
import com.warehouse.security.LoginUser;
import com.warehouse.security.UserContext;
import com.warehouse.service.BizStocktakingService;
import com.warehouse.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 盘点单控制器。
 */
@RestController
@RequestMapping("/api/stocktaking")
@RequiredArgsConstructor
public class BizStocktakingController {

    private final BizStocktakingService stocktakingService;
    private final SysLogService logService;

    @GetMapping("/page")
    public Result<PageResult<BizStocktaking>> page(@RequestParam(defaultValue = "1") long pageNum,
                                                   @RequestParam(defaultValue = "10") long pageSize,
                                                   @RequestParam(required = false) Integer status,
                                                   @RequestParam(required = false) Long warehouseId) {
        IPage<BizStocktaking> page = stocktakingService.pageQuery(pageNum, pageSize, status, warehouseId);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @GetMapping("/{id}")
    public Result<BizStocktaking> detail(@PathVariable Long id) {
        return Result.success(stocktakingService.detail(id));
    }

    /** 按仓库当前库存新建盘点单 */
    @PostMapping("/create")
    public Result<Long> create(@RequestParam Long warehouseId,
                               @RequestParam(required = false) String handlerName,
                               @RequestParam(required = false) String remark) {
        return Result.success(stocktakingService.createByWarehouse(warehouseId, handlerName, remark));
    }

    /** 保存实盘数量 */
    @PutMapping("/{id}/actual")
    public Result<Void> saveActual(@PathVariable Long id, @RequestBody BizStocktaking order) {
        stocktakingService.saveActual(id, order);
        return Result.success();
    }

    /** 完成盘点：按盈亏差异调整库存 */
    @PostMapping("/{id}/finish")
    public Result<Void> finish(@PathVariable Long id) {
        stocktakingService.finish(id);
        LoginUser user = UserContext.get();
        logService.record(user == null ? null : user.getUsername(), "库存盘点", "完成盘点并调整库存",
                "POST /api/stocktaking/{id}/finish", "盘点单ID=" + id, 0L);
        return Result.success();
    }
}
