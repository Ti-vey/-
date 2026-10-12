package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存：按「仓库 + 物料 + 批次」三个维度记录数量与成本。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_stock")
public class BizStock extends BaseEntity {

    /** 仓库主键 */
    private Long warehouseId;

    /** 物料主键 */
    private Long materialId;

    /** 批次号 */
    private String batchNo;

    /** 当前库存数量 */
    private BigDecimal quantity;

    /** 锁定数量 */
    private BigDecimal lockedQty;

    /** 移动加权平均成本单价 */
    private BigDecimal avgCost;

    /** 有效期截止日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expireDate;

    /** 最近入库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastInTime;

    /** 最近出库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastOutTime;

    /** 非数据库字段：物料编码 */
    @TableField(exist = false)
    private String materialCode;

    /** 非数据库字段：物料名称 */
    @TableField(exist = false)
    private String materialName;

    /** 非数据库字段：物料规格 */
    @TableField(exist = false)
    private String spec;

    /** 非数据库字段：计量单位名称 */
    @TableField(exist = false)
    private String unitName;

    /** 非数据库字段：仓库名称 */
    @TableField(exist = false)
    private String warehouseName;

    /** 非数据库字段：安全库存，用于列表判断是否低库存 */
    @TableField(exist = false)
    private BigDecimal safetyStock;

    /** 非数据库字段：库存金额（数量 × 平均成本） */
    @TableField(exist = false)
    private BigDecimal amount;
}
