package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 库存流水：记录每一次库存变动，是进销存报表的数据来源。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_stock_flow")
public class BizStockFlow extends BaseEntity {

    /** 流水号 */
    private String flowNo;

    /** 仓库主键 */
    private Long warehouseId;

    /** 物料主键 */
    private Long materialId;

    /** 流水类型：1 入库，2 出库，3 调拨入，4 调拨出，5 盘盈，6 盘亏 */
    private Integer flowType;

    /** 变动数量 */
    private BigDecimal changeQty;

    /** 变动前库存 */
    private BigDecimal beforeQty;

    /** 变动后库存 */
    private BigDecimal afterQty;

    /** 批次号 */
    private String batchNo;

    /** 关联单据编号 */
    private String refNo;

    /** 关联单据主键 */
    private Long refId;

    /** 操作人 */
    private String operator;

    /** 备注 */
    private String remark;

    /** 非数据库字段：物料名称 */
    @TableField(exist = false)
    private String materialName;

    /** 非数据库字段：仓库名称 */
    @TableField(exist = false)
    private String warehouseName;
}
