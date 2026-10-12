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
 * 入库单主表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inbound")
public class BizInbound extends BaseEntity {

    /** 入库单号 */
    private String inboundNo;

    /** 入库类型：1 采购入库，2 退货入库，3 其他入库 */
    private Integer inboundType;

    /** 入库仓库主键 */
    private Long warehouseId;

    /** 供应商主键 */
    private Long supplierId;

    /** 制单人主键 */
    private Long handlerId;

    /** 制单人姓名 */
    private String handlerName;

    /** 入库总金额 */
    private BigDecimal totalAmount;

    /** 单据状态：0 草稿，1 待审核，2 已入库，3 已驳回 */
    private Integer status;

    /** 审核人 */
    private String auditUser;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /** 审核意见 */
    private String auditRemark;

    /** 实际入库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inboundTime;

    /** 备注 */
    private String remark;

    /** 非数据库字段：仓库名称 */
    @TableField(exist = false)
    private String warehouseName;

    /** 非数据库字段：供应商名称 */
    @TableField(exist = false)
    private String supplierName;

    /** 非数据库字段：入库明细列表 */
    @TableField(exist = false)
    private List<BizInboundItem> items;
}
