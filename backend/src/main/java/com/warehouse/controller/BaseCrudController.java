package com.warehouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.common.BaseEntity;
import com.warehouse.common.PageResult;
import com.warehouse.common.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 基础数据通用控制器。
 *
 * 计量单位、物料分类、仓库、库位、供应商、客户、公告这七类基础数据的增删改查逻辑完全一致，
 * 因此把公共实现抽取到抽象基类中，子类只需要声明自己使用的业务接口，
 * 既减少了重复代码，也便于统一维护接口规范。
 *
 * @param <T> 实体类型
 * @param <S> 业务接口类型
 */
public abstract class BaseCrudController<T extends BaseEntity, S extends IService<T>> {

    /** 由子类提供具体的业务接口实现 */
    protected abstract S service();

    /** 分页查询 */
    @GetMapping("/page")
    public Result<PageResult<T>> page(@RequestParam(defaultValue = "1") long pageNum,
                                      @RequestParam(defaultValue = "10") long pageSize) {
        IPage<T> page = service().page(new Page<>(pageNum, pageSize));
        return Result.success(PageResult.of(page.getTotal(), page.getRecords()));
    }

    /** 查询全部（用于下拉选择） */
    @GetMapping("/list")
    public Result<List<T>> list() {
        return Result.success(service().list());
    }

    /** 按主键查询 */
    @GetMapping("/{id}")
    public Result<T> detail(@PathVariable Long id) {
        return Result.success(service().getById(id));
    }

    /** 新增 */
    @PostMapping
    public Result<Long> save(@RequestBody T entity) {
        service().save(entity);
        return Result.success(entity.getId());
    }

    /** 修改 */
    @PutMapping
    public Result<Void> update(@RequestBody T entity) {
        service().updateById(entity);
        return Result.success();
    }

    /** 删除（逻辑删除） */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        service().removeById(id);
        return Result.success();
    }
}
