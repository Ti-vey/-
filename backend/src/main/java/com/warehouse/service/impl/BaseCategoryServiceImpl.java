package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.BaseCategory;
import com.warehouse.mapper.BaseCategoryMapper;
import com.warehouse.service.BaseCategoryService;
import org.springframework.stereotype.Service;

/** 物料分类业务实现 */
@Service
public class BaseCategoryServiceImpl extends ServiceImpl<BaseCategoryMapper, BaseCategory> implements BaseCategoryService {

    /**
     * 删除分类前校验：存在子分类时不允许删除。
     */
    @Override
    public boolean removeById(java.io.Serializable id) {
        long children = this.count(new LambdaQueryWrapper<BaseCategory>()
                .eq(BaseCategory::getParentId, id));
        if (children > 0) {
            throw new com.warehouse.common.BusinessException("该分类下存在子分类，无法删除");
        }
        return super.removeById(id);
    }
}
