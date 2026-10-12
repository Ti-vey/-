package com.warehouse.service;

import java.util.List;
import java.util.Map;

/**
 * 统计分析业务接口：为首页看板与报表页面提供聚合数据。
 */
public interface StatService {

    /** 首页看板概览数据 */
    Map<String, Object> dashboard();

    /** 近 N 天出入库趋势 */
    List<Map<String, Object>> inoutTrend(int days);

    /** 库存金额 Top N 物料 */
    List<Map<String, Object>> topMaterials(int limit);

    /** 低于安全库存的物料清单，用于首页提醒 */
    List<Map<String, Object>> lowStockList(int limit);

    /** 临近有效期的批次清单，用于首页提醒 */
    List<Map<String, Object>> expiringList(int limit);
}
