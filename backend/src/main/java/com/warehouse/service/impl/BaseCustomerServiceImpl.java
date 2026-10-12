package com.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.BaseCustomer;
import com.warehouse.mapper.BaseCustomerMapper;
import com.warehouse.service.BaseCustomerService;
import org.springframework.stereotype.Service;

/** 客户业务实现 */
@Service
public class BaseCustomerServiceImpl extends ServiceImpl<BaseCustomerMapper, BaseCustomer> implements BaseCustomerService {
}
