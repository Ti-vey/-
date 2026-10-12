package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物料分类。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_category")
public class BaseCategory extends BaseEntity {

    /** 分类编码 */
    private String categoryCode;

    /** 分类名称 */
    private String categoryName;

    /** 上级分类主键，0 表示顶级分类 */
    private Long parentId;

    /** 排序号 */
    private Integer sortNo;

    /** 备注 */
    private String remark;
}
