package com.warehouse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.BizStocktaking;

/** 盘点单业务接口 */
public interface BizStocktakingService extends IService<BizStocktaking> {

    /** 分页查询盘点单 */
    IPage<BizStocktaking> pageQuery(long pageNum, long pageSize, Integer status, Long warehouseId);

    /** 查询盘点单详情（含明细） */
    BizStocktaking detail(Long id);

    /** 按当前库存生成盘点单草稿 */
    Long createByWarehouse(Long warehouseId, String handlerName, String remark);

    /** 保存实盘数量 */
    void saveActual(Long id, BizStocktaking order);

    /** 完成盘点：按盈亏差异调整库存并写入盘盈/盘亏流水 */
    void finish(Long id);
}
