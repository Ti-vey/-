package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 计量单位。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_unit")
public class BaseUnit extends BaseEntity {

    /** 单位名称 */
    private String unitName;

    /** 单位编码 */
    private String unitCode;
}
