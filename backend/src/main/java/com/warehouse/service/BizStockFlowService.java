package com.warehouse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.BizStockFlow;

/** 库存流水业务接口 */
public interface BizStockFlowService extends IService<BizStockFlow> {

    /** 分页查询库存流水 */
    IPage<BizStockFlow> pageQuery(long pageNum, long pageSize, Long materialId, Long warehouseId, Integer flowType);
}
