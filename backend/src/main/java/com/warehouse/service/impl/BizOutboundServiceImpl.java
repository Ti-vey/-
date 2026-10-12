package com.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.warehouse.common.BusinessException;
import com.warehouse.entity.BaseCustomer;
import com.warehouse.entity.BaseMaterial;
import com.warehouse.entity.BaseUnit;
import com.warehouse.entity.BaseWarehouse;
import com.warehouse.entity.BizOutbound;
import com.warehouse.entity.BizOutboundItem;
import com.warehouse.mapper.BaseCustomerMapper;
import com.warehouse.mapper.BaseMaterialMapper;
import com.warehouse.mapper.BaseUnitMapper;
import com.warehouse.mapper.BaseWarehouseMapper;
import com.warehouse.mapper.BizOutboundItemMapper;
import com.warehouse.mapper.BizOutboundMapper;
import com.warehouse.security.LoginUser;
import com.warehouse.security.UserContext;
import com.warehouse.service.BizOutboundService;
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
 * 出库单业务实现。
 * 与入库单相反，出库在审核通过时会扣减库存；扣减前先做库存充足性校验，
 * 真正扣减时再依赖数据库条件更新兜底，保证不出现负库存。
 */
@Service
@RequiredArgsConstructor
public class BizOutboundServiceImpl extends ServiceImpl<BizOutboundMapper, BizOutbound> implements BizOutboundService {

    private final BizOutboundItemMapper itemMapper;
    private final BaseWarehouseMapper warehouseMapper;
    private final BaseCustomerMapper customerMapper;
    private final BaseMaterialMapper materialMapper;
    private final BaseUnitMapper unitMapper;
    private final BizStockService stockService;

    @Override
    public IPage<BizOutbound> pageQuery(long pageNum, long pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<BizOutbound> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), BizOutbound::getOutboundNo, keyword)
                .eq(status != null, BizOutbound::getStatus, status)
                .orderByDesc(BizOutbound::getId);
        IPage<BizOutbound> page = this.page(new Page<>(pageNum, pageSize), wrapper);
        fillNames(page.getRecords());
        return page;
    }

    @Override
    public BizOutbound detail(Long id) {
        BizOutbound order = this.getById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在或已被删除");
        }
        List<BizOutboundItem> items = itemMapper.selectList(new LambdaQueryWrapper<BizOutboundItem>()
                .eq(BizOutboundItem::getOutboundId, id));
        fillItemNames(items);
        // 附带当前可用库存，便于前端在编辑时提示
        items.forEach(item -> item.setStockQty(stockService.getAvailableQty(order.getWarehouseId(), item.getMaterialId())));
        order.setItems(items);
        fillNames(Collections.singletonList(order));
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrder(BizOutbound order) {
        if (order.getWarehouseId() == null) {
            throw new BusinessException("请选择出库仓库");
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BusinessException("请至少录入一条出库明细");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (BizOutboundItem item : order.getItems()) {
            if (item.getMaterialId() == null) {
                throw new BusinessException("出库明细缺少物料信息");
            }
            if (item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("出库数量必须大于 0");
            }
            BigDecimal price = item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice();
            item.setAmount(item.getQuantity().multiply(price).setScale(2, RoundingMode.HALF_UP));
            total = total.add(item.getAmount());
        }
        order.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));

        if (order.getId() == null) {
            if (!StringUtils.hasText(order.getOutboundNo())) {
                order.setOutboundNo(OrderNoUtil.generate("OUT"));
            }
            order.setStatus(0);
            LoginUser user = UserContext.get();
            if (user != null) {
                order.setHandlerId(user.getUserId());
                order.setHandlerName(user.getRealName());
            }
            this.save(order);
        } else {
            BizOutbound exist = this.getById(order.getId());
            if (exist == null) {
                throw new BusinessException("出库单不存在");
            }
            if (exist.getStatus() != 0 && exist.getStatus() != 3) {
                throw new BusinessException("只有草稿或已驳回的单据才能修改");
            }
            order.setStatus(0);
            this.updateById(order);
            itemMapper.delete(new LambdaQueryWrapper<BizOutboundItem>()
                    .eq(BizOutboundItem::getOutboundId, order.getId()));
        }

        for (BizOutboundItem item : order.getItems()) {
            item.setId(null);
            item.setOutboundId(order.getId());
            itemMapper.insert(item);
        }
        return order.getId();
    }

    @Override
    public void submit(Long id) {
        BizOutbound order = this.getById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getStatus() != 0 && order.getStatus() != 3) {
            throw new BusinessException("当前状态不允许提交审核");
        }
        long itemCount = itemMapper.selectCount(new LambdaQueryWrapper<BizOutboundItem>()
                .eq(BizOutboundItem::getOutboundId, id));
        if (itemCount == 0) {
            throw new BusinessException("出库单没有明细，无法提交审核");
        }
        BizOutbound update = new BizOutbound();
        update.setId(id);
        update.setStatus(1);
        this.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long id, boolean pass, String remark) {
        BizOutbound order = this.getById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("只有待审核的单据才能执行审核操作");
        }

        String auditUser = currentUserName();
        BizOutbound update = new BizOutbound();
        update.setId(id);
        update.setAuditUser(auditUser);
        update.setAuditTime(LocalDateTime.now());

        if (!pass) {
            update.setStatus(3);
            update.setAuditRemark(StringUtils.hasText(remark) ? remark : "审核不通过");
            this.updateById(update);
            return;
        }

        List<BizOutboundItem> items = itemMapper.selectList(new LambdaQueryWrapper<BizOutboundItem>()
                .eq(BizOutboundItem::getOutboundId, id));

        // 先整体校验一遍库存，避免扣减到一半才发现某条明细不足
        for (BizOutboundItem item : items) {
            BigDecimal available = stockService.getAvailableQty(order.getWarehouseId(), item.getMaterialId());
            if (available.compareTo(item.getQuantity()) < 0) {
                BaseMaterial material = materialMapper.selectById(item.getMaterialId());
                String name = material == null ? ("物料#" + item.getMaterialId()) : material.getMaterialName();
                throw new BusinessException("【" + name + "】可用库存不足，当前可用 "
                        + available.stripTrailingZeros().toPlainString()
                        + "，本次需要 " + item.getQuantity().stripTrailingZeros().toPlainString());
            }
        }

        for (BizOutboundItem item : items) {
            stockService.decreaseStock(order.getWarehouseId(), item.getMaterialId(), item.getBatchNo(),
                    item.getQuantity(), 2, order.getOutboundNo(), order.getId(), auditUser, "出库单审核通过");
        }

        update.setStatus(2);
        update.setAuditRemark(remark);
        update.setOutboundTime(LocalDateTime.now());
        this.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeOrder(Long id) {
        BizOutbound order = this.getById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (order.getStatus() == 2) {
            throw new BusinessException("已出库的单据不允许删除");
        }
        if (order.getStatus() == 1) {
            throw new BusinessException("待审核的单据请先驳回后再删除");
        }
        itemMapper.delete(new LambdaQueryWrapper<BizOutboundItem>().eq(BizOutboundItem::getOutboundId, id));
        this.removeById(id);
    }

    private void fillNames(List<BizOutbound> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        Map<Long, String> warehouses = warehouseMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseWarehouse::getId, BaseWarehouse::getWarehouseName, (a, b) -> a));
        Map<Long, String> customers = customerMapper.selectList(null).stream()
                .collect(Collectors.toMap(BaseCustomer::getId, BaseCustomer::getCustomerName, (a, b) -> a));
        orders.forEach(order -> {
            order.setWarehouseName(warehouses.get(order.getWarehouseId()));
            order.setCustomerName(customers.get(order.getCustomerId()));
        });
    }

    private void fillItemNames(List<BizOutboundItem> items) {
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
