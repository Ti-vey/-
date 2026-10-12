package com.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.BaseWarehouse;
import com.warehouse.mapper.BaseWarehouseMapper;
import com.warehouse.service.BaseWarehouseService;
import org.springframework.stereotype.Service;

/** 仓库业务实现 */
@Service
public class BaseWarehouseServiceImpl extends ServiceImpl<BaseWarehouseMapper, BaseWarehouse> implements BaseWarehouseService {
}
