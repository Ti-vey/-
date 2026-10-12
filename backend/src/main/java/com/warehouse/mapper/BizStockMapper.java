package com.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.warehouse.entity.BizStock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/** 库存 Mapper */
public interface BizStockMapper extends BaseMapper<BizStock> {

    /**
     * 库存分页查询：关联物料、单位与仓库表，一次性取出列表所需的展示字段。
     */
    @Select("<script>"
            + "SELECT s.*, m.material_code, m.material_name, m.spec, m.safety_stock, "
            + "u.unit_name, w.warehouse_name "
            + "FROM biz_stock s "
            + "LEFT JOIN base_material m ON s.material_id = m.id AND m.deleted = 0 "
            + "LEFT JOIN base_unit u ON m.unit_id = u.id AND u.deleted = 0 "
            + "LEFT JOIN base_warehouse w ON s.warehouse_id = w.id AND w.deleted = 0 "
            + "WHERE s.deleted = 0 "
            + "<if test='keyword != null and keyword != &quot;&quot;'>"
            + "  AND (m.material_name LIKE CONCAT('%', #{keyword}, '%') "
            + "       OR m.material_code LIKE CONCAT('%', #{keyword}, '%')) "
            + "</if>"
            + "<if test='warehouseId != null'> AND s.warehouse_id = #{warehouseId} </if>"
            + "<if test='onlyWarning != null and onlyWarning'> AND s.quantity &lt;= m.safety_stock </if>"
            + "ORDER BY m.material_code ASC, s.warehouse_id ASC"
            + "</script>")
    IPage<BizStock> selectStockPage(Page<BizStock> page,
                                    @Param("keyword") String keyword,
                                    @Param("warehouseId") Long warehouseId,
                                    @Param("onlyWarning") Boolean onlyWarning);

    /**
     * 按物料维度汇总库存：跨仓库合并同一物料的数量与金额。
     */
    @Select("SELECT m.id AS materialId, m.material_code AS materialCode, m.material_name AS materialName, "
            + "m.spec AS spec, u.unit_name AS unitName, m.safety_stock AS safetyStock, "
            + "IFNULL(SUM(s.quantity), 0) AS quantity, "
            + "IFNULL(SUM(s.quantity * s.avg_cost), 0) AS amount, "
            + "COUNT(DISTINCT s.warehouse_id) AS warehouseCount "
            + "FROM biz_stock s "
            + "LEFT JOIN base_material m ON s.material_id = m.id AND m.deleted = 0 "
            + "LEFT JOIN base_unit u ON m.unit_id = u.id AND u.deleted = 0 "
            + "WHERE s.deleted = 0 "
            + "GROUP BY m.id, m.material_code, m.material_name, m.spec, u.unit_name, m.safety_stock "
            + "ORDER BY amount DESC")
    List<Map<String, Object>> selectSummaryByMaterial();

    /**
     * 按仓库维度汇总库存：统计各仓库的物料种类数、库存总量与库存金额。
     */
    @Select("SELECT w.id AS warehouseId, w.warehouse_name AS warehouseName, "
            + "COUNT(DISTINCT s.material_id) AS materialCount, "
            + "IFNULL(SUM(s.quantity), 0) AS quantity, "
            + "IFNULL(SUM(s.quantity * s.avg_cost), 0) AS amount "
            + "FROM biz_stock s "
            + "LEFT JOIN base_warehouse w ON s.warehouse_id = w.id AND w.deleted = 0 "
            + "WHERE s.deleted = 0 "
            + "GROUP BY w.id, w.warehouse_name "
            + "ORDER BY amount DESC")
    List<Map<String, Object>> selectSummaryByWarehouse();
}
