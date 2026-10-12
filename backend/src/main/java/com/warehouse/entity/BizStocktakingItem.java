package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 盘点单明细：记录账面数量、实盘数量与盈亏差异。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_stocktaking_item")
public class BizStocktakingItem extends BaseEntity {

    /** 盘点单主键 */
    private Long stocktakingId;

    /** 物料主键 */
    private Long materialId;

    /** 批次号 */
    private String batchNo;

    /** 账面数量 */
    private BigDecimal bookQty;

    /** 实盘数量 */
    private BigDecimal actualQty;

    /** 盈亏数量：正数盘盈，负数盘亏 */
    private BigDecimal diffQty;

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
}
