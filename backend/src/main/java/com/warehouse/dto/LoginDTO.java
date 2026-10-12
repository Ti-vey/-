package com.warehouse.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求参数。
 */
@Data
public class LoginDTO {

    /** 登录账号 */
    @NotBlank(message = "请输入登录账号")
    private String username;

    /** 登录密码 */
    @NotBlank(message = "请输入登录密码")
    private String password;
}
