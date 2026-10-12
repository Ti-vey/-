package com.warehouse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.BizOutbound;

/** 出库单业务接口 */
public interface BizOutboundService extends IService<BizOutbound> {

    /** 分页查询出库单 */
    IPage<BizOutbound> pageQuery(long pageNum, long pageSize, String keyword, Integer status);

    /** 查询出库单详情（含明细） */
    BizOutbound detail(Long id);

    /** 保存出库单（新增或修改） */
    Long saveOrder(BizOutbound order);

    /** 提交审核 */
    void submit(Long id);

    /**
     * 审核出库单。
     * 审核通过时先校验库存是否充足，再扣减库存并写入流水。
     */
    void audit(Long id, boolean pass, String remark);

    /** 删除草稿或已驳回的单据 */
    void removeOrder(Long id);
}
