package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.common.BusinessException;
import com.warehouse.entity.BaseMaterial;
import com.warehouse.entity.BaseUnit;
import com.warehouse.entity.BaseWarehouse;
import com.warehouse.entity.BizStock;
import com.warehouse.entity.BizStocktaking;
import com.warehouse.entity.BizStocktakingItem;
import com.warehouse.mapper.BaseMaterialMapper;
import com.warehouse.mapper.BaseUnitMapper;
import com.warehouse.mapper.BaseWarehouseMapper;
import com.warehouse.mapper.BizStocktakingItemMapper;
import com.warehouse.mapper.BizStocktakingMapper;
import com.warehouse.security.LoginUser;
import com.warehouse.security.UserContext;
import com.warehouse.service.BizStockService;
import com.warehouse.service.BizStocktakingService;
import com.warehouse.util.OrderNoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 盘点单业务实现。
 *
 * 盘点分两步：先按当前账面库存生成盘点单，录入实盘数量；
 * 完成盘点时按「实盘 - 账面」的差异调整库存，盘盈走流水类型 5，盘亏走类型 6。
 */
@Service
@RequiredArgsConstructor
public class BizStocktakingServiceImpl extends ServiceImpl<BizStocktakingMapper, BizStocktaking>
        implements BizStocktakingService {

    private final BizStocktakingItemMapper itemMapper;
    private final BaseWarehouseMapper warehouseMapper;
    private final BaseMaterialMapper materialMapper;
    private final BaseUnitMapper unitMapper;
    private final BizStockService stockService;

    @Override
    public IPage<BizStocktaking> pageQuery(long pageNum, long pageSize, Integer status, Long warehouseId) {
        LambdaQueryWrapper<BizStocktaking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, BizStocktaking::getStatus, status)
                .eq(warehouseId != null, BizStocktaking::getWarehouseId, warehouseId)
                .orderByDesc(BizStocktaking::getId);
        IPage<BizStocktaking> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        fillNames(page.getRecords());
        return page;
    }

    @Override
    public BizStocktaking detail(Long id) {
        BizStocktaking order = this.getById(id);
        if (order == null) {
            throw new BusinessException("盘点单不存在或已被删除");
        }
        List<BizStocktakingItem> items = itemMapper.selectList(new LambdaQueryWrapper<BizStocktakingItem>()
                .eq(BizStocktakingItem::getStocktakingId, id));
        fillItemNames(items);
        order.setItems(items);
        fillNames(Collections.singletonList(order));
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createByWarehouse(Long warehouseId, String handlerName, String remark) {
        List<BizStock> stocks = stockService.list(new LambdaQueryWrapper<BizStock>()
                .eq(BizStock::getWarehouseId, warehouseId));
        if (stocks.isEmpty()) {
            throw new BusinessException("该仓库暂无库存记录，无需盘点");
        }

        BizStocktaking order = new BizStocktaking();
        order.setStocktakingNo(OrderNoUtil.generate("PD"));
        order.setWarehouseId(warehouseId);
        order.setStatus(0);
        order.setHandlerName(StringUtils.hasText(handlerName) ? handlerName : currentUserName());
        order.setProfitQty(BigDecimal.ZERO);
        order.setLossQty(BigDecimal.ZERO);
        order.setRemark(remark);
        this.save(order);

        for (BizStock stock : stocks) {
            BizStocktakingItem item = new BizStocktakingItem();
            item.setStocktakingId(order.getId());
            item.setMaterialId(stock.getMaterialId());
            item.setBatchNo(stock.getBatchNo());
            item.setBookQty(stock.getQuantity());
            // 实盘数量默认等于账面数量，由盘点人后续修改
            item.setActualQty(stock.getQuantity());
            item.setDiffQty(BigDecimal.ZERO);
            itemMapper.insert(item);
        }
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveActual(Long id, BizStocktaking order) {
        BizStocktaking exist = this.getById(id);
        if (exist == null) {
            throw new BusinessException("盘点单不存在");
        }
        if (exist.getStatus() != 0) {
            throw new BusinessException("已完成的盘点单不允许修改");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BusinessException("盘点明细不能为空");
        }

        BigDecimal profit = BigDecimal.ZERO;
        BigDecimal loss = BigDecimal.ZERO;
        for (BizStocktakingItem item : order.getItems()) {
            BizStocktakingItem dbItem = itemMapper.selectById(item.getId());
            if (dbItem == null) {
                continue;
            }
            BigDecimal actual = item.getActualQty() == null ? BigDecimal.ZERO : item.getActualQty();
            BigDecimal diff = actual.subtract(dbItem.getBookQty() == null ? BigDecimal.ZERO : dbItem.getBookQty());
            dbItem.setActualQty(actual);
            dbItem.setDiffQty(diff);
            dbItem.setRemark(item.getRemark());
            itemMapper.updateById(dbItem);

            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                profit = profit.add(diff);
            } else {
                loss = loss.add(diff.abs());
            }
        }

        BizStocktaking update = new BizStocktaking();
        update.setId(id);
        update.setProfitQty(profit.setScale(2, RoundingMode.HALF_UP));
        update.setLossQty(loss.setScale(2, RoundingMode.HALF_UP));
        if (StringUtils.hasText(order.getRemark())) {
            update.setRemark(order.getRemark());
        }
        this.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(Long id) {
        BizStocktaking order = this.getById(id);
        if (order == null) {
            throw new BusinessException("盘点单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("该盘点单已完成，请勿重复提交");
        }

        String operator = currentUserName();
        List<BizStocktakingItem> items = itemMapper.selectList(new LambdaQueryWrapper<BizStocktakingItem>()
                .eq(BizStocktakingItem::getStocktakingId, id));

        for (BizStocktakingItem item : items) {
            BigDecimal diff = item.getDiffQty() == null ? BigDecimal.ZERO : item.getDiffQty();
            if (diff.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                // 盘盈：按现有成本单价补充库存
                BizStock stock = stockService.getOne(new LambdaQueryWrapper<BizStock>()
                        .eq(BizStock::getWarehouseId, order.getWarehouseId())
                        .eq(BizStock::getMaterialId, item.getMaterialId())
                        .eq(BizStock::getBatchNo, item.getBatchNo())
                        .last("limit 1"));
                BigDecimal cost = stock == null ? BigDecimal.ZERO : stock.getAvgCost();
                stockService.increaseStock(order.getWarehouseId(), item.getMaterialId(), item.getBatchNo(),
                        diff, cost, stock == null ? null : stock.getExpireDate(), 5,
                        order.getStocktakingNo(), order.getId(), operator, "盘点盘盈调整");
            } else {
                stockService.decreaseStock(order.getWarehouseId(), item.getMaterialId(), item.getBatchNo(),
                        diff.abs(), 6, order.getStocktakingNo(), order.getId(), operator, "盘点盘亏调整");
            }
        }

        BizStocktaking update = new BizStocktaking();
        update.setId(id);
        update.setStatus(1);
        update.setStocktakingTime(LocalDateTime.now());
        this.updateById(update);
    }

    private void fillNames(List<BizStocktaking> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        Map<Long, String> warehouses = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getWarehouseName, (a, b) -> a));
        orders.forEach(order -> order.setWarehouseName(warehouses.get(order.getWarehouseId())));
    }

    private void fillItemNames(List<BizStocktakingItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Map<Long, BaseMaterial> materials = materialMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseMaterial::getId, Function.identity(), (a, b) -> a));
        Map<Long, String> units = unitMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseUnit::getId, BaseUnit::getUnitName, (a, b) -> a));
        items.forEach(item -> {
            BaseMaterial material = materials.get(item.getMaterialId());
            if (material != null) {
                item.setMaterialCode(material.getMaterialCode());
                item.setMaterialName(material.getMaterialName());
                item.setSpec(material.getSpec());
                item.setUnitName(units.get(material.getUnitId()));
            }
        });
    }

    private String currentUserName() {
        LoginUser user = UserContext.get();
        return user == null ? "系统" : user.getRealName();
    }
}
