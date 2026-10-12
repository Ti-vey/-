package com.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.BaseUnit;
import com.warehouse.mapper.BaseUnitMapper;
import com.warehouse.service.BaseUnitService;
import org.springframework.stereotype.Service;

/** 计量单位业务实现 */
@Service
public class BaseUnitServiceImpl extends ServiceImpl<BaseUnitMapper, BaseUnit> implements BaseUnitService {
}
