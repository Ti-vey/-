package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_customer")
public class BaseCustomer extends BaseEntity {

    /** 客户编码 */
    private String customerCode;

    /** 客户名称 */
    private String customerName;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String phone;

    /** 地址 */
    private String address;

    /** 状态：1 合作中，0 已停用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
