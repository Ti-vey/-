package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 调拨单主表：实现仓库之间的库存转移。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_transfer")
public class BizTransfer extends BaseEntity {

    /** 调拨单号 */
    private String transferNo;

    /** 调出仓库主键 */
    private Long fromWarehouseId;

    /** 调入仓库主键 */
    private Long toWarehouseId;

    /** 单据状态：0 草稿，1 待审核，2 已完成，3 已驳回 */
    private Integer status;

    /** 制单人主键 */
    private Long handlerId;

    /** 制单人姓名 */
    private String handlerName;

    /** 审核人 */
    private String auditUser;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /** 调拨完成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transferTime;

    /** 备注 */
    private String remark;

    /** 非数据库字段：调出仓库名称 */
    @TableField(exist = false)
    private String fromWarehouseName;

    /** 非数据库字段：调入仓库名称 */
    @TableField(exist = false)
    private String toWarehouseName;

    /** 非数据库字段：调拨明细列表 */
    @TableField(exist = false)
    private List<BizTransferItem> items;
}
