package com.warehouse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.BizTransfer;

/** 调拨单业务接口 */
public interface BizTransferService extends IService<BizTransfer> {

    /** 分页查询调拨单 */
    IPage<BizTransfer> pageQuery(long pageNum, long pageSize, String keyword, Integer status);

    /** 查询调拨单详情（含明细） */
    BizTransfer detail(Long id);

    /** 保存调拨单 */
    Long saveOrder(BizTransfer order);

    /** 提交审核 */
    void submit(Long id);

    /** 审核调拨单，通过时完成调出仓库扣减与调入仓库增加 */
    void audit(Long id, boolean pass, String remark);

    /** 删除草稿或已驳回的单据 */
    void removeOrder(Long id);
}
