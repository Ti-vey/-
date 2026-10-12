package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库位。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_location")
public class BaseLocation extends BaseEntity {

    /** 库位编码 */
    private String locationCode;

    /** 库位名称 */
    private String locationName;

    /** 所属仓库主键 */
    private Long warehouseId;

    /** 状态：1 可用，0 停用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
