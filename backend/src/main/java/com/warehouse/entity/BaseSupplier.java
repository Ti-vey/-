package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 供应商。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_supplier")
public class BaseSupplier extends BaseEntity {

    /** 供应商编码 */
    private String supplierCode;

    /** 供应商名称 */
    private String supplierName;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String phone;

    /** 地址 */
    private String address;

    /** 邮箱 */
    private String email;

    /** 状态：1 合作中，0 已停用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
