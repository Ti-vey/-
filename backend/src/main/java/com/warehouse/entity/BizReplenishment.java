package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 补货计划：由「安全库存模型 + EOQ 经济订货批量」计算生成的补货建议。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_replenishment")
public class BizReplenishment extends BaseEntity {

    /** 补货计划编号 */
    private String replenishNo;

    /** 物料主键 */
    private Long materialId;

    /** 仓库主键 */
    private Long warehouseId;

    /** 当前库存数量 */
    private BigDecimal currentQty;

    /** 安全库存 */
    private BigDecimal safetyStock;

    /** 建议补货数量 */
    private BigDecimal suggestQty;

    /** 经济订货批量 */
    private BigDecimal eoqQty;

    /** 平均日消耗量 */
    private BigDecimal avgDailyUsage;

    /** 补货提前期（天） */
    private Integer leadTime;

    /** 紧急程度：1 紧急，2 一般，3 低 */
    private Integer urgency;

    /** 状态：0 待处理，1 已生成采购，2 已忽略 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 非数据库字段：物料编码 */
    @TableField(exist = false)
    private String materialCode;

    /** 非数据库字段：物料名称 */
    @TableField(exist = false)
    private String materialName;

    /** 非数据库字段：单位 */
    @TableField(exist = false)
    private String unitName;

    /** 非数据库字段：仓库名称 */
    @TableField(exist = false)
    private String warehouseName;

    /** 非数据库字段：ABC 分类 */
    @TableField(exist = false)
    private String abcType;
}
