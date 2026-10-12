package com.warehouse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.BizStockWarning;

/** 库存预警业务接口 */
public interface BizStockWarningService extends IService<BizStockWarning> {

    /** 分页查询预警记录 */
    IPage<BizStockWarning> pageQuery(long pageNum, long pageSize, Integer warningType, Integer handled);

    /**
     * 执行预警扫描：低于安全库存、呆滞物料、临近保质期。
     *
     * @return 新增预警记录数
     */
    int generateWarnings();

    /** 处理预警 */
    void handle(Long id, String handleRemark);
}
