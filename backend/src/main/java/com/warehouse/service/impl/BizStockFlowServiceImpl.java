package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.entity.BaseMaterial;
import com.warehouse.entity.BaseWarehouse;
import com.warehouse.entity.BizStockFlow;
import com.warehouse.mapper.BaseMaterialMapper;
import com.warehouse.mapper.BaseWarehouseMapper;
import com.warehouse.mapper.BizStockFlowMapper;
import com.warehouse.service.BizStockFlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 库存流水业务实现 */
@Service
@RequiredArgsConstructor
public class BizStockFlowServiceImpl extends ServiceImpl<BizStockFlowMapper, BizStockFlow> implements BizStockFlowService {

    private final BaseMaterialMapper materialMapper;
    private final BaseWarehouseMapper warehouseMapper;

    @Override
    public IPage<BizStockFlow> pageQuery(long pageNum, long pageSize, Long materialId, Long warehouseId, Integer flowType) {
        LambdaQueryWrapper<BizStockFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(materialId != null, BizStockFlow::getMaterialId, materialId)
                .eq(warehouseId != null, BizStockFlow::getWarehouseId, warehouseId)
                .eq(flowType != null, BizStockFlow::getFlowType, flowType)
                .orderByDesc(BizStockFlow::getId);

        IPage<BizStockFlow> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        List<BizStockFlow> records = page.getRecords();
        if (records.isEmpty()) {
            return page;
        }

        Map<Long, String> materialNames = materialMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseMaterial::getId, BaseMaterial::getMaterialName, (a, b) -> a));
        Map<Long, String> warehouseNames = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getWarehouseName, (a, b) -> a));
        records.forEach(flow -> {
            flow.setMaterialName(materialNames.get(flow.getMaterialId()));
            flow.setWarehouseName(warehouseNames.get(flow.getWarehouseId()));
        });
        return page;
    }
}
