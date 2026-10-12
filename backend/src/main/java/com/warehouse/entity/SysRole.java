package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /** 角色编码 */
    private String roleCode;

    /** 角色名称 */
    private String roleName;

    /** 角色说明 */
    private String description;
}
