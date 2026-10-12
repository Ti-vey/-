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
import com.warehouse.entity.BizTransfer;
import com.warehouse.entity.BizTransferItem;
import com.warehouse.mapper.BaseMaterialMapper;
import com.warehouse.mapper.BaseUnitMapper;
import com.warehouse.mapper.BaseWarehouseMapper;
import com.warehouse.mapper.BizTransferItemMapper;
import com.warehouse.mapper.BizTransferMapper;
import com.warehouse.security.LoginUser;
import com.warehouse.security.UserContext;
import com.warehouse.service.BizStockService;
import com.warehouse.service.BizTransferService;
import com.warehouse.util.OrderNoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 调拨单业务实现。
 *
 * 调拨是「一出一入」的组合动作：审核通过时先从调出仓库扣减（流水类型 4），
 * 再向调入仓库增加（流水类型 3）。两个动作处于同一事务，任一步失败都会整体回滚，
 * 避免出现「货已调出但未调入」的中间状态。
 */
@Service
@RequiredArgsConstructor
public class BizTransferServiceImpl extends ServiceImpl<BizTransferMapper, BizTransfer> implements BizTransferService {

    private static final String DEFAULT_BATCH = "DEFAULT";

    private final BizTransferItemMapper itemMapper;
    private final BaseWarehouseMapper warehouseMapper;
    private final BaseMaterialMapper materialMapper;
    private final BaseUnitMapper unitMapper;
    private final BizStockService stockService;

    @Override
    public IPage<BizTransfer> pageQuery(long pageNum, long pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<BizTransfer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), BizTransfer::getTransferNo, keyword)
                .eq(status != null, BizTransfer::getStatus, status)
                .orderByDesc(BizTransfer::getId);
        IPage<BizTransfer> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        fillNames(page.getRecords());
        return page;
    }

    @Override
    public BizTransfer detail(Long id) {
        BizTransfer order = this.getById(id);
        if (order == null) {
            throw new BusinessException("调拨单不存在或已被删除");
        }
        List<BizTransferItem> items = itemMapper.selectList(new LambdaQueryWrapper<BizTransferItem>()
                .eq(BizTransferItem::getTransferId, id));
        fillItemNames(items);
        items.forEach(item -> item.setStockQty(
                stockService.getAvailableQty(order.getFromWarehouseId(), item.getMaterialId())));
        order.setItems(items);
        fillNames(Collections.singletonList(order));
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrder(BizTransfer order) {
        if (order.getFromWarehouseId() == null || order.getToWarehouseId() == null) {
            throw new BusinessException("请选择调出仓库与调入仓库");
        }
        if (order.getFromWarehouseId().equals(order.getToWarehouseId())) {
            throw new BusinessException("调出仓库与调入仓库不能相同");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BusinessException("请至少录入一条调拨明细");
        }
        for (BizTransferItem item : order.getItems()) {
            if (item.getMaterialId() == null || item.getQuantity() == null
                    || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("调拨明细的物料与数量不能为空，且数量必须大于 0");
            }
        }

        if (order.getId() == null) {
            if (!StringUtils.hasText(order.getTransferNo())) {
                order.setTransferNo(OrderNoUtil.generate("TR"));
            }
            order.setStatus(0);
            LoginUser user = UserContext.get();
            if (user != null) {
                order.setHandlerId(user.getUserId());
                order.setHandlerName(user.getRealName());
            }
            this.save(order);
        } else {
            BizTransfer exist = this.getById(order.getId());
            if (exist == null) {
                throw new BusinessException("调拨单不存在");
            }
            if (exist.getStatus() != 0 && exist.getStatus() != 3) {
                throw new BusinessException("只有草稿或已驳回的单据才能修改");
            }
            order.setStatus(0);
            this.updateById(order);
            itemMapper.delete(new LambdaQueryWrapper<BizTransferItem>()
                    .eq(BizTransferItem::getTransferId, order.getId()));
        }

        for (BizTransferItem item : order.getItems()) {
            item.setId(null);
            item.setTransferId(order.getId());
            itemMapper.insert(item);
        }
        return order.getId();
    }

    @Override
    public void submit(Long id) {
        BizTransfer order = this.getById(id);
        if (order == null) {
            throw new BusinessException("调拨单不存在");
        }
        if (order.getStatus() != 0 && order.getStatus() != 3) {
            throw new BusinessException("当前状态不允许提交审核");
        }
        long itemCount = itemMapper.selectCount(new LambdaQueryWrapper<BizTransferItem>()
                .eq(BizTransferItem::getTransferId, id));
        if (itemCount == 0) {
            throw new BusinessException("调拨单没有明细，无法提交审核");
        }
        BizTransfer update = new BizTransfer();
        update.setId(id);
        update.setStatus(1);
        this.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, boolean pass, String remark) {
        BizTransfer order = this.getById(id);
        if (order == null) {
            throw new BusinessException("调拨单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("只有待审核的单据才能执行审核操作");
        }

        String auditUser = currentUserName();
        BizTransfer update = new BizTransfer();
        update.setId(id);
        update.setAuditUser(auditUser);
        update.setAuditTime(LocalDateTime.now());

        if (!pass) {
            update.setStatus(3);
            this.updateById(update);
            return;
        }

        List<BizTransferItem> items = itemMapper.selectList(new LambdaQueryWrapper<BizTransferItem>()
                .eq(BizTransferItem::getTransferId, id));
        for (BizTransferItem item : items) {
            BigDecimal available = stockService.getAvailableQty(order.getFromWarehouseId(), item.getMaterialId());
            if (available.compareTo(item.getQuantity()) < 0) {
                BaseMaterial material = materialMapper.selectById(item.getMaterialId());
                String name = material == null ? ("物料#" + item.getMaterialId()) : material.getMaterialName();
                throw new BusinessException("【" + name + "】调出仓库可用库存不足，当前可用 "
                        + available.stripTrailingZeros().toPlainString());
            }
        }

        for (BizTransferItem item : items) {
            String batch = StringUtils.hasText(item.getBatchNo()) ? item.getBatchNo() : DEFAULT_BATCH;
            // 取调出仓库当前成本单价，调入时保持价值一致
            BizStock sourceStock = stockService.getOne(new LambdaQueryWrapper<BizStock>()
                    .eq(BizStock::getWarehouseId, order.getFromWarehouseId())
                    .eq(BizStock::getMaterialId, item.getMaterialId())
                    .eq(BizStock::getBatchNo, batch)
                    .last("limit 1"));
            BigDecimal avgCost = sourceStock == null ? BigDecimal.ZERO : sourceStock.getAvgCost();

            stockService.decreaseStock(order.getFromWarehouseId(), item.getMaterialId(), batch,
                    item.getQuantity(), 4, order.getTransferNo(), order.getId(), auditUser, "调拨出库");
            stockService.increaseStock(order.getToWarehouseId(), item.getMaterialId(), batch,
                    item.getQuantity(), avgCost, sourceStock == null ? null : sourceStock.getExpireDate(),
                    3, order.getTransferNo(), order.getId(), auditUser, "调拨入库");
        }

        update.setStatus(2);
        update.setTransferTime(LocalDateTime.now());
        this.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeOrder(Long id) {
        BizTransfer order = this.getById(id);
        if (order == null) {
            throw new BusinessException("调拨单不存在");
        }
        if (order.getStatus() == 2) {
            throw new BusinessException("已完成的调拨单不允许删除");
        }
        if (order.getStatus() == 1) {
            throw new BusinessException("待审核的单据请先驳回后再删除");
        }
        itemMapper.delete(new LambdaQueryWrapper<BizTransferItem>().eq(BizTransferItem::getTransferId, id));
        this.removeById(id);
    }

    private void fillNames(List<BizTransfer> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        Map<Long, String> warehouses = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getWarehouseName, (a, b) -> a));
        orders.forEach(order -> {
            order.setFromWarehouseName(warehouses.get(order.getFromWarehouseId()));
            order.setToWarehouseName(warehouses.get(order.getToWarehouseId()));
        });
    }

    private void fillItemNames(List<BizTransferItem> items) {
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
