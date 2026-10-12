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
 * 出库单主表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_outbound")
public class BizOutbound extends BaseEntity {

    /** 出库单号 */
    private String outboundNo;

    /** 出库类型：1 销售出库，2 领用出库，3 其他出库 */
    private Integer outboundType;

    /** 出库仓库主键 */
    private Long warehouseId;

    /** 客户主键 */
    private Long customerId;

    /** 制单人主键 */
    private Long handlerId;

    /** 制单人姓名 */
    private String handlerName;

    /** 出库总金额 */
    private BigDecimal totalAmount;

    /** 单据状态：0 草稿，1 待审核，2 已出库，3 已驳回 */
    private Integer status;

    /** 审核人 */
    private String auditUser;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    /** 审核意见 */
    private String auditRemark;

    /** 实际出库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime outboundTime;

    /** 备注 */
    private String remark;

    /** 非数据库字段：仓库名称 */
    @TableField(exist = false)
    private String warehouseName;

    /** 非数据库字段：客户名称 */
    @TableField(exist = false)
    private String customerName;

    /** 非数据库字段：出库明细列表 */
    @TableField(exist = false)
    private List<BizOutboundItem> items;
}
