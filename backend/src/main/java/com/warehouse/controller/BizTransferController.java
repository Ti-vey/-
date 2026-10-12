package com.warehouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.dto.AuditDTO;
import com.warehouse.entity.BizTransfer;
import com.warehouse.security.LoginUser;
import com.warehouse.security.UserContext;
import com.warehouse.service.BizTransferService;
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
 * 调拨单控制器。
 */
@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class BizTransferController {

    private final BizTransferService transferService;
    private final SysLogService logService;

    @GetMapping("/page")
    public Result<PageResult<BizTransfer>> page(@RequestParam(defaultValue = "1") long pageNum,
                                                @RequestParam(defaultValue = "10") long pageSize,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) Integer status) {
        IPage<BizTransfer> page = transferService.pageQuery(pageNum, pageSize, keyword, status);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    @GetMapping("/{id}")
    public Result<BizTransfer> detail(@PathVariable Long id) {
        return Result.success(transferService.detail(id));
    }

    @PostMapping
    public Result<Long> save(@RequestBody BizTransfer order) {
        return Result.success(transferService.saveOrder(order));
    }

    @PutMapping
    public Result<Long> update(@RequestBody BizTransfer order) {
        return Result.success(transferService.saveOrder(order));
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        transferService.submit(id);
        return Result.success();
    }

    /** 审核调拨单：通过后完成仓库间的库存转移 */
    @PostMapping("/audit")
    public Result<Void> audit(@RequestBody @Valid AuditDTO dto) {
        transferService.audit(dto.getId(), Boolean.TRUE.equals(dto.getPass()), dto.getRemark());
        LoginUser user = UserContext.get();
        logService.record(user == null ? null : user.getUsername(), "库存调拨",
                Boolean.TRUE.equals(dto.getPass()) ? "审核通过调拨单" : "驳回调拨单",
                "POST /api/transfer/audit", "单据ID=" + dto.getId(), 0L);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        transferService.removeOrder(id);
        return Result.success();
    }
}
