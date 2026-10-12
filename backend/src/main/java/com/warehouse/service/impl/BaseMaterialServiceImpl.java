package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.BaseMaterial;
import com.warehouse.mapper.BaseMaterialMapper;
import com.warehouse.service.BaseMaterialService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/** 物料档案业务实现 */
@Service
public class BaseMaterialServiceImpl extends ServiceImpl<BaseMaterialMapper, BaseMaterial> implements BaseMaterialService {

    @Override
    public IPage<BaseMaterial> pageQuery(long pageNum, long pageSize, String keyword,
                                         Long categoryId, String abcType, Integer status) {
        LambdaQueryWrapper<BaseMaterial> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BaseMaterial::getMaterialName, keyword)
                    .or().like(BaseMaterial::getMaterialCode, keyword));
        }
        wrapper.eq(categoryId != null, BaseMaterial::getCategoryId, categoryId)
                .eq(StringUtils.hasText(abcType), BaseMaterial::getAbcType, abcType)
                .eq(status != null, BaseMaterial::getStatus, status)
                .orderByAsc(BaseMaterial::getMaterialCode);
        return this.page(new Page<>(pageNum, pageSize), wrapper);
    }
}
