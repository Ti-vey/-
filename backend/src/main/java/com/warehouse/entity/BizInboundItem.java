package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 入库单明细。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inbound_item")
public class BizInboundItem extends BaseEntity {

    /** 入库单主键 */
    private Long inboundId;

    /** 物料主键 */
    private Long materialId;

    /** 入库数量 */
    private BigDecimal quantity;

    /** 入库单价 */
    private BigDecimal unitPrice;

    /** 金额小计 */
    private BigDecimal amount;

    /** 批次号 */
    private String batchNo;

    /** 生产日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate produceDate;

    /** 有效期截止日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDate;

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
