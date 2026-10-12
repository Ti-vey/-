package com.warehouse.controller;

import com.warehouse.entity.BaseCategory;
import com.warehouse.service.BaseCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 物料分类控制器 */
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class BaseCategoryController extends BaseCrudController<BaseCategory, BaseCategoryService> {

    private final BaseCategoryService baseCategoryService;

    @Override
    protected BaseCategoryService service() {
        return baseCategoryService;
    }
}
