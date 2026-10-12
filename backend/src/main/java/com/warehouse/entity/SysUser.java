package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统用户。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /** 登录账号 */
    private String username;

    /** 密码（BCrypt 密文），只允许写入不允许返回给前端 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 联系电话 */
    private String phone;

    /** 电子邮箱 */
    private String email;

    /** 性别：1 男，2 女 */
    private Integer gender;

    /** 账号状态：1 启用，0 禁用 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 非数据库字段：角色主键 */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private Long roleId;

    /** 非数据库字段：角色名称 */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String roleName;
}
