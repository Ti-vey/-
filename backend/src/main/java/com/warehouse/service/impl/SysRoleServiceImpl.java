package com.warehouse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.SysRole;
import com.warehouse.mapper.SysRoleMapper;
import com.warehouse.service.SysRoleService;
import org.springframework.stereotype.Service;

/** 角色业务实现 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
}
