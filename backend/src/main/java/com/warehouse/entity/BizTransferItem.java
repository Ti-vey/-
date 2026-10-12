package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 调拨单明细。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_transfer_item")
public class BizTransferItem extends BaseEntity {

    /** 调拨单主键 */
    private Long transferId;

    /** 物料主键 */
    private Long materialId;

    /** 调拨数量 */
    private BigDecimal quantity;

    /** 批次号 */
    private String batchNo;

    /** 备注 */
    private String remark;

    /** 非数据库字段：物料编码 */
    @TableField(exist = false)
    private String materialCode;

    /** 非数据库字段：物料名称 */
    @TableField(exist = false)
    private String materialName;

    /** 非数据库字段：规格 */
    @TableField(exist = false)
    private String spec;

    /** 非数据库字段：单位 */
    @TableField(exist = false)
    private String unitName;

    /** 非数据库字段：调出仓库该物料可用库存 */
    @TableField(exist = false)
    private BigDecimal stockQty;
}
