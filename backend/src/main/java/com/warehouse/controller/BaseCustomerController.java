package com.warehouse.controller;

import com.warehouse.entity.BaseCustomer;
import com.warehouse.service.BaseCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 客户控制器 */
@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class BaseCustomerController extends BaseCrudController<BaseCustomer, BaseCustomerService> {

    private final BaseCustomerService baseCustomerService;

    @Override
    protected BaseCustomerService service() {
        return baseCustomerService;
    }
}
