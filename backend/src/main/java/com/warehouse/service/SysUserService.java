package com.warehouse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.warehouse.entity.SysUser;
import com.warehouse.vo.LoginVO;

/** 用户业务接口 */
public interface SysUserService extends IService<SysUser> {

    /** 登录校验，成功返回令牌与用户信息 */
    LoginVO login(String username, String password);

    /** 分页查询用户 */
    IPage<SysUser> pageQuery(long pageNum, long pageSize, String keyword, Integer status);

    /** 新增用户，密码使用 BCrypt 加密并绑定角色 */
    void saveUser(SysUser user, Long roleId);

    /** 修改用户信息，密码为空时不修改密码 */
    void updateUser(SysUser user, Long roleId);

    /** 重置密码为 123456 */
    void resetPassword(Long userId);

    /** 修改账号状态 */
    void changeStatus(Long userId, Integer status);
}
