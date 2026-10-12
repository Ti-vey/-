package com.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.BaseLocation;
import com.warehouse.mapper.BaseLocationMapper;
import com.warehouse.service.BaseLocationService;
import org.springframework.stereotype.Service;

/** 库位业务实现 */
@Service
public class BaseLocationServiceImpl extends ServiceImpl<BaseLocationMapper, BaseLocation> implements BaseLocationService {
}
