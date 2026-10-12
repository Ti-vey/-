package com.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 物料档案，是库存、单据与补货计算的核心主数据。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_material")
public class BaseMaterial extends BaseEntity {

    /** 物料编码 */
    private String materialCode;

    /** 物料名称 */
    private String materialName;

    /** 所属分类主键 */
    private Long categoryId;

    /** 计量单位主键 */
    private Long unitId;

    /** 规格 */
    private String spec;

    /** 型号 */
    private String model;

    /** 品牌 */
    private String brand;

    /** 安全库存下限 */
    private BigDecimal safetyStock;

    /** 库存上限 */
    private BigDecimal maxStock;

    /** 参考采购单价 */
    private BigDecimal purchasePrice;

    /** 参考出库单价 */
    private BigDecimal salePrice;

    /** 保质期天数，0 表示不管控 */
    private Integer shelfLifeDays;

    /** ABC 分类：A、B、C */
    private String abcType;

    /** 状态：1 启用，0 停用 */
    private Integer status;

    /** 备注 */
    private String remark;
}
