package com.warehouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.BizReplenishment;
import com.warehouse.security.LoginUser;
import com.warehouse.security.UserContext;
import com.warehouse.service.BizReplenishmentService;
import com.warehouse.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 补货计划控制器（核心创新点：ABC 分类 + 安全库存模型 + EOQ 经济订货批量）。
 */
@RestController
@RequestMapping("/api/replenishment")
@RequiredArgsConstructor
public class BizReplenishmentController {

    private final BizReplenishmentService replenishmentService;
    private final SysLogService logService;

    @GetMapping("/page")
    public Result<PageResult<BizReplenishment>> page(@RequestParam(defaultValue = "1") long pageNum,
                                                     @RequestParam(defaultValue = "10") long pageSize,
                                                     @RequestParam(required = false) Integer status,
                                                     @RequestParam(required = false) Integer urgency) {
        IPage<BizReplenishment> page = replenishmentService.pageQuery(pageNum, pageSize, status, urgency);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 触发补货建议计算：扫描库存并生成补货计划 */
    @PostMapping("/generate")
    public Result<List<BizReplenishment>> generate() {
        long start = System.currentTimeMillis();
        List<BizReplenishment> list = replenishmentService.generate();
        LoginUser user = UserContext.get();
        logService.record(user == null ? null : user.getUsername(), "智能补货", "生成补货建议",
                "POST /api/replenishment/generate", "生成 " + list.size() + " 条建议",
                System.currentTimeMillis() - start);
        return Result.success("共生成 " + list.size() + " 条补货建议", list);
    }

    /** 更新补货计划状态：1 已生成采购，2 已忽略 */
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id,
                                     @RequestParam Integer status,
                                     @RequestParam(required = false) String remark) {
        replenishmentService.changeStatus(id, status, remark);
        return Result.success();
    }
}
