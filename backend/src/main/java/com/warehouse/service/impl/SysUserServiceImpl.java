package com.warehouse.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.common.BusinessException;
import com.warehouse.entity.SysRole;
import com.warehouse.entity.SysUser;
import com.warehouse.entity.SysUserRole;
import com.warehouse.mapper.SysRoleMapper;
import com.warehouse.mapper.SysUserMapper;
import com.warehouse.mapper.SysUserRoleMapper;
import com.warehouse.security.JwtUtil;
import com.warehouse.service.SysUserService;
import com.warehouse.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 用户业务实现 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    /** 重置密码后的默认密码 */
    private static final String DEFAULT_PASSWORD = "123456";

    private final JwtUtil jwtUtil;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;

    @Override
    public LoginVO login(String username, String password) {
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("账号或密码错误");
        }
        // 使用 BCrypt 校验密文，数据库中不保存明文密码
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系系统管理员");
        }

        SysRole role = findRole(user.getId());
        String roleCode = role == null ? "KEEPER" : role.getRoleCode();
        String roleName = role == null ? "仓管员" : role.getRoleName();

        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.createToken(user.getId(), user.getUsername(), user.getRealName(), roleCode));
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRoleCode(roleCode);
        vo.setRoleName(roleName);
        return vo;
    }

    @Override
    public IPage<SysUser> pageQuery(long pageNum, long pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }
        wrapper.eq(status != null, SysUser::getStatus, status)
                .orderByAsc(SysUser::getId);

        IPage<SysUser> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        fillRole(page.getRecords());
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUser(SysUser user, Long roleId) {
        long exists = this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()));
        if (exists > 0) {
            throw new BusinessException("登录账号已存在");
        }
        String rawPassword = StringUtils.hasText(user.getPassword()) ? user.getPassword() : DEFAULT_PASSWORD;
        user.setPassword(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        this.save(user);
        bindRole(user.getId(), roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(SysUser user, Long roleId) {
        SysUser current = this.getById(user.getId());
        if (current == null) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.hasText(user.getUsername())
                && !current.getUsername().equals(user.getUsername())) {
            long exists = this.count(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, user.getUsername())
                    .ne(SysUser::getId, user.getId()));
            if (exists > 0) {
                throw new BusinessException("登录账号已存在");
            }
        }
        // 修改用户时不通过该接口修改密码，避免把明文直接覆盖进数据库
        user.setPassword(null);
        user.setUpdateTime(java.time.LocalDateTime.now());
        this.updateById(user);
        bindRole(user.getId(), roleId);
    }

    @Override
    public void resetPassword(Long userId) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setPassword(BCrypt.hashpw(DEFAULT_PASSWORD, BCrypt.gensalt()));
        this.updateById(user);
    }

    @Override
    public void changeStatus(Long userId, Integer status) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setStatus(status);
        this.updateById(user);
    }

    /** 查询用户对应角色 */
    private SysRole findRole(Long userId) {
        SysUserRole relation = userRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId).last("limit 1"));
        if (relation == null) {
            return null;
        }
        return roleMapper.selectById(relation.getRoleId());
    }

    /** 重新绑定用户角色：先删除原有关联再插入 */
    private void bindRole(Long userId, Long roleId) {
        if (roleId == null) {
            return;
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        SysUserRole relation = new SysUserRole();
        relation.setUserId(userId);
        relation.setRoleId(roleId);
        userRoleMapper.insert(relation);
    }

    /** 批量填充角色名称，避免列表逐行查询数据库 */
    private void fillRole(List<SysUser> users) {
        if (users == null || users.isEmpty()) {
            return;
        }
        List<Long> userIds = users.stream().map(SysUser::getId).collect(Collectors.toList());
        List<SysUserRole> relations = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
        if (relations.isEmpty()) {
            return;
        }
        List<SysRole> roles = roleMapper.selectBatchIds(
                relations.stream().map(SysUserRole::getRoleId).distinct().collect(Collectors.toList()));
        Map<Long, SysRole> roleMap = roles.stream().collect(Collectors.toMap(SysRole::getId, Function.identity()));
        Map<Long, SysUserRole> relationMap = new HashMap<>();
        relations.forEach(r -> relationMap.putIfAbsent(r.getUserId(), r));

        users.forEach(user -> {
            SysUserRole relation = relationMap.get(user.getId());
            if (relation != null) {
                SysRole role = roleMap.get(relation.getRoleId());
                if (role != null) {
                    user.setRoleId(role.getId());
                    user.setRoleName(role.getRoleName());
                }
            }
        });
    }
}
