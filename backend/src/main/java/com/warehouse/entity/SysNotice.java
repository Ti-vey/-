package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统公告。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class SysNotice extends BaseEntity {

    /** 公告标题 */
    private String title;

    /** 公告内容 */
    private String content;

    /** 发布人 */
    private String publisher;

    /** 状态：1 已发布，0 草稿 */
    private Integer status;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;
}
