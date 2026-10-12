package com.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.BaseSupplier;
import com.warehouse.mapper.BaseSupplierMapper;
import com.warehouse.service.BaseSupplierService;
import org.springframework.stereotype.Service;

/** 供应商业务实现 */
@Service
public class BaseSupplierServiceImpl extends ServiceImpl<BaseSupplierMapper, BaseSupplier> implements BaseSupplierService {
}
