package com.warehouse.security;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前登录用户信息，由 JWT 解析得到。
 */
@Data
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户主键 */
    private Long userId;
    /** 登录账号 */
    private String username;
    /** 用户姓名 */
    private String realName;
    /** 角色编码：ADMIN 系统管理员，MANAGER 仓库主管，KEEPER 仓管员 */
    private String roleCode;
}
