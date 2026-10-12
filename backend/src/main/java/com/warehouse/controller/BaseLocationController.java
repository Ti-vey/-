package com.warehouse.controller;

import com.warehouse.entity.BaseLocation;
import com.warehouse.service.BaseLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 库位控制器 */
@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class BaseLocationController extends BaseCrudController<BaseLocation, BaseLocationService> {

    private final BaseLocationService baseLocationService;

    @Override
    protected BaseLocationService service() {
        return baseLocationService;
    }
}
