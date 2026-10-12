package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户与角色的关联关系。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_role")
public class SysUserRole extends BaseEntity {

    /** 用户主键 */
    private Long userId;

    /** 角色主键 */
    private Long roleId;
}
