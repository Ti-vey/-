package com.warehouse.controller;

import com.warehouse.common.Result;
import com.warehouse.service.StatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 统计分析控制器：首页看板与报表数据。
 */
@RestController
@RequestMapping("/api/stat")
@RequiredArgsConstructor
public class StatController {

    private final StatService statService;

    /** 首页看板概览 */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        return Result.success(statService.dashboard());
    }

    /** 近 N 天出入库趋势 */
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> trend(@RequestParam(defaultValue = "7") int days) {
        return Result.success(statService.inoutTrend(days));
    }

    /** 库存金额 Top N 物料 */
    @GetMapping("/top-materials")
    public Result<List<Map<String, Object>>> topMaterials(@RequestParam(defaultValue = "10") int limit) {
        return Result.success(statService.topMaterials(limit));
    }

    /** 低于安全库存的物料清单（首页提醒） */
    @GetMapping("/low-stock")
    public Result<List<Map<String, Object>>> lowStock(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(statService.lowStockList(limit));
    }

    /** 临近有效期的批次清单（首页提醒） */
    @GetMapping("/expiring")
    public Result<List<Map<String, Object>>> expiring(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(statService.expiringList(limit));
    }
}
