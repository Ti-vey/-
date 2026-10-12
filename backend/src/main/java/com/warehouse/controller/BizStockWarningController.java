package com.warehouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.BizStockWarning;
import com.warehouse.security.LoginUser;
import com.warehouse.security.UserContext;
import com.warehouse.service.BizStockWarningService;
import com.warehouse.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存预警控制器。
 */
@RestController
@RequestMapping("/api/warning")
@RequiredArgsConstructor
public class BizStockWarningController {

    private final BizStockWarningService warningService;
    private final SysLogService logService;

    @GetMapping("/page")
    public Result<PageResult<BizStockWarning>> page(@RequestParam(defaultValue = "1") long pageNum,
                                                    @RequestParam(defaultValue = "10") long pageSize,
                                                    @RequestParam(required = false) Integer warningType,
                                                    @RequestParam(required = false) Integer handled) {
        IPage<BizStockWarning> page = warningService.pageQuery(pageNum, pageSize, warningType, handled);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 执行预警扫描 */
    @PostMapping("/generate")
    public Result<Integer> generate() {
        int count = warningService.generateWarnings();
        LoginUser user = UserContext.get();
        logService.record(user == null ? null : user.getUsername(), "库存预警", "执行预警扫描",
                "POST /api/warning/generate", "新增 " + count + " 条预警", 0L);
        return Result.success("本次共生成 " + count + " 条预警记录", count);
    }

    /** 处理预警 */
    @PutMapping("/{id}/handle")
    public Result<Void> handle(@PathVariable Long id, @RequestParam(required = false) String remark) {
        warningService.handle(id, remark);
        return Result.success();
    }
}
