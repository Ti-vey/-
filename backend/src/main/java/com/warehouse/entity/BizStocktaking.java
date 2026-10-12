package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 盘点单主表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_stocktaking")
public class BizStocktaking extends BaseEntity {

    /** 盘点单号 */
    private String stocktakingNo;

    /** 盘点仓库主键 */
    private Long warehouseId;

    /** 状态：0 盘点中，1 已完成 */
    private Integer status;

    /** 盘点人 */
    private String handlerName;

    /** 盘盈总数 */
    private BigDecimal profitQty;

    /** 盘亏总数 */
    private BigDecimal lossQty;

    /** 盘点完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime stocktakingTime;

    /** 备注 */
    private String remark;

    /** 非数据库字段：仓库名称 */
    @TableField(exist = false)
    private String warehouseName;

    /** 非数据库字段：盘点明细列表 */
    @TableField(exist = false)
    private List<BizStocktakingItem> items;
}
