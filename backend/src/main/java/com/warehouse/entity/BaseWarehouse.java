package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 仓库。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_warehouse")
public class BaseWarehouse extends BaseEntity {

    /** 仓库编码 */
    private String warehouseCode;

    /** 仓库名称 */
    private String warehouseName;

    /** 仓库地址 */
    private String address;

    /** 仓库负责人 */
    private String manager;

    /** 联系电话 */
    private String phone;

    /** 状态：1 启用，0 停用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
