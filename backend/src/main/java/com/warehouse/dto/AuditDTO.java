package com.warehouse.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 单据审核请求参数。
 */
@Data
public class AuditDTO {

    /** 单据主键 */
    @NotNull(message = "缺少单据编号")
    private Long id;

    /** 是否通过 */
    @NotNull(message = "请选择审核结果")
    private Boolean pass;

    /** 审核意见 */
    private String remark;
}
