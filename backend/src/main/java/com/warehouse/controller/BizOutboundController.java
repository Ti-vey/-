package com.warehouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.dto.AuditDTO;
import com.warehouse.entity.BizOutbound;
import com.warehouse.security.LoginUser;
import com.warehouse.security.UserContext;
import com.warehouse.service.BizOutboundService;
import com.warehouse.service.SysLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 出库单控制器。
 */
@RestController
@RequestMapping("/api/outbound")
@RequiredArgsConstructor
public class BizOutboundController {

    private final BizOutboundService outboundService;
    private final SysLogService logService;

    @GetMapping("/page")
    public Result<PageResult<BizOutbound>> page(@RequestParam(defaultValue = "1") long pageNum,
                                                @RequestParam(defaultValue = "10") long pageSize,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer status) {
        IPage<BizOutbound> page = outboundService.pageQuery(pageNum, pageSize, keyword, status);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @GetMapping("/{id}")
    public Result<BizOutbound> detail(@PathVariable Long id) {
        return Result.success(outboundService.detail(id));
    }

    @PostMapping
    public Result<Long> save(@RequestBody BizOutbound order) {
        return Result.success(outboundService.saveOrder(order));
    }

    @PutMapping
    public Result<Long> update(@RequestBody BizOutbound order) {
        return Result.success(outboundService.saveOrder(order));
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        outboundService.submit(id);
        return Result.success();
    }

    /** 审核出库单：通过后扣减库存 */
    @PostMapping("/audit")
    public Result<Void> audit(@RequestBody @Valid AuditDTO dto) {
        outboundService.audit(dto.getId(), Boolean.TRUE.equals(dto.getPass()), dto.getRemark());
        LoginUser user = UserContext.get();
        logService.record(user == null ? null : user.getUsername(), "出库管理",
                Boolean.TRUE.equals(dto.getPass()) ? "审核通过出库单" : "驳回出库单",
                "POST /api/outbound/audit", "单据ID=" + dto.getId(), 0L);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        outboundService.removeOrder(id);
        return Result.success();
    }
}
