package com.warehouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.dto.AuditDTO;
import com.warehouse.entity.BizInbound;
import com.warehouse.security.LoginUser;
import com.warehouse.security.UserContext;
import com.warehouse.service.BizInboundService;
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
 * 入库单控制器。
 */
@RestController
@RequestMapping("/api/inbound")
@RequiredArgsConstructor
public class BizInboundController {

    private final BizInboundService inboundService;
    private final SysLogService logService;

    /** 分页查询入库单 */
    @GetMapping("/page")
    public Result<PageResult<BizInbound>> page(@RequestParam(defaultValue = "1") long pageNum,
                                               @RequestParam(defaultValue = "10") long pageSize,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Integer status) {
        IPage<BizInbound> page = inboundService.pageQuery(pageNum, pageSize, keyword, status);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 入库单详情（含明细） */
    @GetMapping("/{id}")
    public Result<BizInbound> detail(@PathVariable Long id) {
        return Result.success(inboundService.detail(id));
    }

    /** 新增入库单 */
    @PostMapping
    public Result<Long> save(@RequestBody BizInbound order) {
        return Result.success(inboundService.saveOrder(order));
    }

    /** 修改入库单 */
    @PutMapping
    public Result<Long> update(@RequestBody BizInbound order) {
        return Result.success(inboundService.saveOrder(order));
    }

    /** 提交审核 */
    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        inboundService.submit(id);
        return Result.success();
    }

    /** 审核入库单：通过后库存增加 */
    @PostMapping("/audit")
    public Result<Void> audit(@RequestBody @Valid AuditDTO dto) {
        inboundService.audit(dto.getId(), Boolean.TRUE.equals(dto.getPass()), dto.getRemark());
        LoginUser user = UserContext.get();
        logService.record(user == null ? null : user.getUsername(), "入库管理",
                Boolean.TRUE.equals(dto.getPass()) ? "审核通过入库单" : "驳回入库单",
                "POST /api/inbound/audit", "单据ID=" + dto.getId(), 0L);
        return Result.success();
    }

    /** 删除入库单 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        inboundService.removeOrder(id);
        return Result.success();
    }
}
