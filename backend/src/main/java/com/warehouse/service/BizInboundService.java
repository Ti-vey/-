package com.warehouse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.BizInbound;

/** 入库单业务接口 */
public interface BizInboundService extends IService<BizInbound> {

    /** 分页查询入库单 */
    IPage<BizInbound> pageQuery(long pageNum, long pageSize, String keyword, Integer status);

    /** 查询入库单详情（含明细） */
    BizInbound detail(Long id);

    /** 保存入库单（新增或修改），自动计算金额与单号 */
    Long saveOrder(BizInbound order);

    /** 提交审核 */
    void submit(Long id);

    /**
     * 审核入库单。
     * 审核通过时增加库存、更新移动加权平均成本并写入库存流水。
     */
    void audit(Long id, boolean pass, String remark);

    /** 删除草稿或已驳回的单据 */
    void removeOrder(Long id);
}
