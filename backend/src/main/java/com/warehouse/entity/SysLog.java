package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 操作日志。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_log")
public class SysLog extends BaseEntity {

    /** 操作人主键 */
    private Long userId;

    /** 操作人账号 */
    private String username;

    /** 业务模块 */
    private String module;

    /** 操作描述 */
    private String operation;

    /** 请求方法 */
    private String method;

    /** 请求参数 */
    private String params;

    /** 客户端 IP */
    private String ip;

    /** 耗时（毫秒） */
    private Long costTime;
}
