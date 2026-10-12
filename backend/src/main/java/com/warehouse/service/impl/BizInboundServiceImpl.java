package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.common.BusinessException;
import com.warehouse.entity.BaseMaterial;
import com.warehouse.entity.BaseSupplier;
import com.warehouse.entity.BaseUnit;
import com.warehouse.entity.BaseWarehouse;
import com.warehouse.entity.BizInbound;
import com.warehouse.entity.BizInboundItem;
import com.warehouse.mapper.BaseMaterialMapper;
import com.warehouse.mapper.BaseSupplierMapper;
import com.warehouse.mapper.BaseUnitMapper;
import com.warehouse.mapper.BaseWarehouseMapper;
import com.warehouse.mapper.BizInboundItemMapper;
import com.warehouse.mapper.BizInboundMapper;
import com.warehouse.security.LoginUser;
import com.warehouse.security.UserContext;
import com.warehouse.service.BizInboundService;
import com.warehouse.service.BizStockService;
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
 * 入库单业务实现。
 * 单据状态流转：0 草稿 → 1 待审核 → 2 已入库 / 3 已驳回。
 * 只有「审核通过」这一动作会真正改变库存，保证账实相符。
 */
@Service
@RequiredArgsConstructor
public class BizInboundServiceImpl extends ServiceImpl<BizInboundMapper, BizInbound> implements BizInboundService {

    private final BizInboundItemMapper itemMapper;
    private final BaseWarehouseMapper warehouseMapper;
    private final BaseSupplierMapper supplierMapper;
    private final BaseMaterialMapper materialMapper;
    private final BaseUnitMapper unitMapper;
    private final BizStockService stockService;

    @Override
    public IPage<BizInbound> pageQuery(long pageNum, long pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<BizInbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), BizInbound::getInboundNo, keyword)
                .eq(status != null, BizInbound::getStatus, status)
                .orderByDesc(BizInbound::getId);
        IPage<BizInbound> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        fillNames(page.getRecords());
        return page;
    }

    @Override
    public BizInbound detail(Long id) {
        BizInbound order = this.getById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在或已被删除");
        }
        List<BizInboundItem> items = itemMapper.selectList(new LambdaQueryWrapper<BizInboundItem>()
                .eq(BizInboundItem::getInboundId, id));
        fillItemNames(items);
        order.setItems(items);
        fillNames(Collections.singletonList(order));
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrder(BizInbound order) {
        if (order.getWarehouseId() == null) {
            throw new BusinessException("请选择入库仓库");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BusinessException("请至少录入一条入库明细");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (BizInboundItem item : order.getItems()) {
            if (item.getMaterialId() == null) {
                throw new BusinessException("入库明细缺少物料信息");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("入库数量必须大于 0");
            }
            BigDecimal price = item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice();
            item.setAmount(item.getQuantity().multiply(price).setScale(2, RoundingMode.HALF_UP));
            total = total.add(item.getAmount());
        }
        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));

        if (order.getId() == null) {
            if (!StringUtils.hasText(order.getInboundNo())) {
                order.setInboundNo(OrderNoUtil.generate("IN"));
            }
            order.setStatus(0);
            LoginUser user = UserContext.get();
            if (user != null) {
                order.setHandlerId(user.getUserId());
                order.setHandlerName(user.getRealName());
            }
            this.save(order);
        } else {
            BizInbound exist = this.getById(order.getId());
            if (exist == null) {
                throw new BusinessException("入库单不存在");
            }
            if (exist.getStatus() != 0 && exist.getStatus() != 3) {
                throw new BusinessException("只有草稿或已驳回的单据才能修改");
            }
            order.setStatus(0);
            this.updateById(order);
            itemMapper.delete(new LambdaQueryWrapper<BizInboundItem>()
                    .eq(BizInboundItem::getInboundId, order.getId()));
        }

        for (BizInboundItem item : order.getItems()) {
            item.setId(null);
            item.setInboundId(order.getId());
            itemMapper.insert(item);
        }
        return order.getId();
    }

    @Override
    public void submit(Long id) {
        BizInbound order = this.getById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (order.getStatus() != 0 && order.getStatus() != 3) {
            throw new BusinessException("当前状态不允许提交审核");
        }
        long itemCount = itemMapper.selectCount(new LambdaQueryWrapper<BizInboundItem>()
                .eq(BizInboundItem::getInboundId, id));
        if (itemCount == 0) {
            throw new BusinessException("入库单没有明细，无法提交审核");
        }
        BizInbound update = new BizInbound();
        update.setId(id);
        update.setStatus(1);
        this.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, boolean pass, String remark) {
        BizInbound order = this.getById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("只有待审核的单据才能执行审核操作");
        }

        String auditUser = currentUserName();
        BizInbound update = new BizInbound();
        update.setId(id);
        update.setAuditUser(auditUser);
        update.setAuditTime(LocalDateTime.now());

        if (!pass) {
            update.setStatus(3);
            update.setAuditRemark(StringUtils.hasText(remark) ? remark : "审核不通过");
            this.updateById(update);
            return;
        }

        List<BizInboundItem> items = itemMapper.selectList(new LambdaQueryWrapper<BizInboundItem>()
                .eq(BizInboundItem::getInboundId, id));
        for (BizInboundItem item : items) {
            // 逐条明细增加库存，并写入类型为「入库」的库存流水
            stockService.increaseStock(order.getWarehouseId(), item.getMaterialId(), item.getBatchNo(),
                    item.getQuantity(), item.getUnitPrice(), item.getExpireDate(),
                    1, order.getInboundNo(), order.getId(), auditUser, "入库单审核通过");
        }

        update.setStatus(2);
        update.setAuditRemark(remark);
        update.setInboundTime(LocalDateTime.now());
        this.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeOrder(Long id) {
        BizInbound order = this.getById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (order.getStatus() == 2) {
            throw new BusinessException("已入库的单据不允许删除");
        }
        if (order.getStatus() == 1) {
            throw new BusinessException("待审核的单据请先驳回后再删除");
        }
        itemMapper.delete(new LambdaQueryWrapper<BizInboundItem>().eq(BizInboundItem::getInboundId, id));
        this.removeById(id);
    }

    /** 填充仓库与供应商名称 */
    private void fillNames(List<BizInbound> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        Map<Long, String> warehouses = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getWarehouseName, (a, b) -> a));
        Map<Long, String> suppliers = supplierMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseSupplier::getId, BaseSupplier::getSupplierName, (a, b) -> a));
        orders.forEach(order -> {
            order.setWarehouseName(warehouses.get(order.getWarehouseId()));
            order.setSupplierName(suppliers.get(order.getSupplierId()));
        });
    }

    /** 填充明细中的物料与单位信息 */
    private void fillItemNames(List<BizInboundItem> items) {
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

    /** 当前登录用户姓名 */
    private String currentUserName() {
        LoginUser user = UserContext.get();
        return user == null ? "系统" : user.getRealName();
    }
}
