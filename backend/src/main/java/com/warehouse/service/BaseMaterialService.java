package com.warehouse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.BaseMaterial;

/** 物料档案业务接口 */
public interface BaseMaterialService extends IService<BaseMaterial> {

    /** 分页查询物料，支持按名称、编码、分类与状态过滤 */
    IPage<BaseMaterial> pageQuery(long pageNum, long pageSize, String keyword, Long categoryId, String abcType, Integer status);
}
