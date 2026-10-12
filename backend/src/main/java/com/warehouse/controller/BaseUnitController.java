package com.warehouse.controller;

import com.warehouse.entity.BaseUnit;
import com.warehouse.service.BaseUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 计量单位控制器 */
@RestController
@RequestMapping("/api/unit")
@RequiredArgsConstructor
public class BaseUnitController extends BaseCrudController<BaseUnit, BaseUnitService> {

    private final BaseUnitService baseUnitService;

    @Override
    protected BaseUnitService service() {
        return baseUnitService;
    }
}
