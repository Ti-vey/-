package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 库存预警记录：低库存、呆滞、临期三类预警。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_stock_warning")
public class BizStockWarning extends BaseEntity {

    /** 仓库主键 */
    private Long warehouseId;

    /** 物料主键 */
    private Long materialId;

    /** 预警类型：1 低于安全库存，2 呆滞物料，3 临近保质期 */
    private Integer warningType;

    /** 预警级别：1 严重，2 警告，3 提示 */
    private Integer warningLevel;

    /** 当前库存数量 */
    private BigDecimal currentQty;

    /** 触发阈值 */
    private BigDecimal thresholdValue;

    /** 预警说明 */
    private String warningMsg;

    /** 处理状态：0 未处理，1 已处理 */
    private Integer handled;

    /** 处理说明 */
    private String handleRemark;

    /** 非数据库字段：物料编码 */
    @TableField(exist = false)
    private String materialCode;

    /** 非数据库字段：物料名称 */
    @TableField(exist = false)
    private String materialName;

    /** 非数据库字段：仓库名称 */
    @TableField(exist = false)
    private String warehouseName;
}
