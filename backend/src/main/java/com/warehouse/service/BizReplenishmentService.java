package com.warehouse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.BizReplenishment;

import java.util.List;

/**
 * 补货计划业务接口。
 * 核心创新点：结合 ABC 分类、安全库存模型与经济订货批量(EOQ)生成补货建议。
 */
public interface BizReplenishmentService extends IService<BizReplenishment> {

    /** 分页查询补货计划 */
    IPage<BizReplenishment> pageQuery(long pageNum, long pageSize, Integer status, Integer urgency);

    /**
     * 扫描全部库存，按安全库存模型与 EOQ 公式生成补货建议。
     *
     * @return 新生成的补货建议列表
     */
    List<BizReplenishment> generate();

    /** 更新补货计划状态 */
    void changeStatus(Long id, Integer status, String remark);
}
