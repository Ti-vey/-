package com.warehouse.controller;

import com.warehouse.entity.BaseWarehouse;
import com.warehouse.service.BaseWarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 仓库控制器 */
@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
public class BaseWarehouseController extends BaseCrudController<BaseWarehouse, BaseWarehouseService> {

    private final BaseWarehouseService baseWarehouseService;

    @Override
    protected BaseWarehouseService service() {
        return baseWarehouseService;
    }
}
