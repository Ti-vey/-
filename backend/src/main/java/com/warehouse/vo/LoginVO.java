package com.warehouse.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录成功后返回给前端的信息。
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 访问令牌 */
    private String token;
    /** 用户主键 */
    private Long userId;
    /** 登录账号 */
    private String username;
    /** 用户姓名 */
    private String realName;
    /** 角色编码 */
    private String roleCode;
    /** 角色名称 */
    private String roleName;
}
