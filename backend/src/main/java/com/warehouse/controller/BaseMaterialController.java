package com.warehouse.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import com.warehouse.entity.BaseMaterial;
import com.warehouse.service.BaseMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 物料档案控制器。
 * 物料是库存业务的核心主数据，查询条件比普通基础数据更多，因此单独实现控制器。
 */
@RestController
@RequestMapping("/api/material")
@RequiredArgsConstructor
public class BaseMaterialController {

    private final BaseMaterialService materialService;

    /** 分页查询：支持关键字、分类、ABC 分类与状态过滤 */
    @GetMapping("/page")
    public Result<PageResult<BaseMaterial>> page(@RequestParam(defaultValue = "1") long pageNum,
                                                 @RequestParam(defaultValue = "10") long pageSize,
                                                 @RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Long categoryId,
                                                 @RequestParam(required = false) String abcType,
                                                 @RequestParam(required = false) Integer status) {
        IPage<BaseMaterial> page = materialService.pageQuery(pageNum, pageSize, keyword, categoryId, abcType, status);
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 下拉选择用的物料列表，只返回启用状态的物料 */
    @GetMapping("/list")
    public Result<List<BaseMaterial>> list(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<BaseMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BaseMaterial::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(BaseMaterial::getMaterialName, keyword)
                    .or().like(BaseMaterial::getMaterialCode, keyword));
        }
        wrapper.orderByAsc(BaseMaterial::getMaterialCode);
        return Result.success(materialService.list(wrapper));
    }

    /** 按主键查询 */
    @GetMapping("/{id}")
    public Result<BaseMaterial> detail(@PathVariable Long id) {
        return Result.success(materialService.getById(id));
    }

    /** 新增物料 */
    @PostMapping
    public Result<Long> save(@RequestBody BaseMaterial material) {
        materialService.save(material);
        return Result.success(material.getId());
    }

    /** 修改物料 */
    @PutMapping
    public Result<Void> update(@RequestBody BaseMaterial material) {
        materialService.updateById(material);
        return Result.success();
    }

    /** 删除物料 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        materialService.removeById(id);
        return Result.success();
    }
}
