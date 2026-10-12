package com.warehouse.controller;

import com.warehouse.entity.BaseSupplier;
import com.warehouse.service.BaseSupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 供应商控制器 */
@RestController
@RequestMapping("/api/supplier")
@RequiredArgsConstructor
public class BaseSupplierController extends BaseCrudController<BaseSupplier, BaseSupplierService> {

    private final BaseSupplierService baseSupplierService;

    @Override
    protected BaseSupplierService service() {
        return baseSupplierService;
    }
}
