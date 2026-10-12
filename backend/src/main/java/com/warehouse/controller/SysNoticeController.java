package com.warehouse.controller;

import com.warehouse.entity.SysNotice;
import com.warehouse.service.SysNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 公告控制器 */
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class SysNoticeController extends BaseCrudController<SysNotice, SysNoticeService> {

    private final SysNoticeService sysNoticeService;

    @Override
    protected SysNoticeService service() {
        return sysNoticeService;
    }
}
