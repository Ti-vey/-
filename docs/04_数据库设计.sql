-- =====================================================================
-- 仓库库存管理系统 数据库脚本
-- 数据库：MySQL 5.7 / 8.0 兼容
-- 字符集：utf8mb4
-- 说明：所有业务表均包含主键 id、创建时间、更新时间与逻辑删除标记
-- =====================================================================

DROP DATABASE IF EXISTS `warehouse_db`;
CREATE DATABASE `warehouse_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `warehouse_db`;

-- =====================================================================
-- 一、系统管理相关表
-- =====================================================================

-- 1. 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(50)  NOT NULL                COMMENT '登录账号',
  `password`    VARCHAR(120) NOT NULL                COMMENT '密码（BCrypt 加密存储）',
  `real_name`   VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
  `phone`       VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
  `email`       VARCHAR(100) DEFAULT NULL            COMMENT '电子邮箱',
  `gender`      TINYINT      DEFAULT 1               COMMENT '性别：1 男，2 女',
  `status`      TINYINT      DEFAULT 1               COMMENT '账号状态：1 启用，0 禁用',
  `remark`      VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      DEFAULT 0               COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2. 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_code`   VARCHAR(50)  NOT NULL                COMMENT '角色编码：ADMIN/MANAGER/KEEPER',
  `role_name`   VARCHAR(50)  NOT NULL                COMMENT '角色名称',
  `description` VARCHAR(255) DEFAULT NULL            COMMENT '角色说明',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT   NOT NULL                COMMENT '用户主键',
  `role_id`     BIGINT   NOT NULL                COMMENT '角色主键',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT  DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 4. 操作日志表
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT       DEFAULT NULL            COMMENT '操作人主键',
  `username`    VARCHAR(50)  DEFAULT NULL            COMMENT '操作人账号',
  `module`      VARCHAR(50)  DEFAULT NULL            COMMENT '业务模块',
  `operation`   VARCHAR(100) DEFAULT NULL            COMMENT '操作描述',
  `method`      VARCHAR(255) DEFAULT NULL            COMMENT '请求方法',
  `params`      TEXT         DEFAULT NULL            COMMENT '请求参数',
  `ip`          VARCHAR(50)  DEFAULT NULL            COMMENT '客户端 IP',
  `cost_time`   BIGINT       DEFAULT NULL            COMMENT '耗时（毫秒）',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT      DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_log_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 5. 系统公告表
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`        VARCHAR(100) NOT NULL                COMMENT '公告标题',
  `content`      TEXT                                 COMMENT '公告内容',
  `publisher`    VARCHAR(50)  DEFAULT NULL            COMMENT '发布人',
  `status`       TINYINT      DEFAULT 1               COMMENT '状态：1 已发布，0 草稿',
  `publish_time` DATETIME     DEFAULT NULL            COMMENT '发布时间',
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT      DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';

-- =====================================================================
-- 二、基础数据表
-- =====================================================================

-- 6. 计量单位表
DROP TABLE IF EXISTS `base_unit`;
CREATE TABLE `base_unit` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  `unit_name`   VARCHAR(30) NOT NULL                COMMENT '单位名称',
  `unit_code`   VARCHAR(30) DEFAULT NULL            COMMENT '单位编码',
  `create_time` DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT     DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计量单位表';

-- 7. 物料分类表
DROP TABLE IF EXISTS `base_category`;
CREATE TABLE `base_category` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `category_code` VARCHAR(50)  DEFAULT NULL            COMMENT '分类编码',
  `category_name` VARCHAR(50)  NOT NULL                COMMENT '分类名称',
  `parent_id`     BIGINT       DEFAULT 0               COMMENT '上级分类主键，0 表示顶级分类',
  `sort_no`       INT          DEFAULT 0               COMMENT '排序号',
  `remark`        VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT      DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_category_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料分类表';

-- 8. 物料档案表
DROP TABLE IF EXISTS `base_material`;
CREATE TABLE `base_material` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `material_code`  VARCHAR(50)   NOT NULL                COMMENT '物料编码',
  `material_name`  VARCHAR(100)  NOT NULL                COMMENT '物料名称',
  `category_id`    BIGINT        DEFAULT NULL            COMMENT '所属分类主键',
  `unit_id`        BIGINT        DEFAULT NULL            COMMENT '计量单位主键',
  `spec`           VARCHAR(100)  DEFAULT NULL            COMMENT '规格',
  `model`          VARCHAR(100)  DEFAULT NULL            COMMENT '型号',
  `brand`          VARCHAR(50)   DEFAULT NULL            COMMENT '品牌',
  `safety_stock`   DECIMAL(18,2) DEFAULT 0               COMMENT '安全库存下限',
  `max_stock`      DECIMAL(18,2) DEFAULT 0               COMMENT '库存上限',
  `purchase_price` DECIMAL(18,2) DEFAULT 0               COMMENT '参考采购单价',
  `sale_price`     DECIMAL(18,2) DEFAULT 0               COMMENT '参考出库单价',
  `shelf_life_days` INT          DEFAULT 0               COMMENT '保质期天数，0 表示不管控保质期',
  `abc_type`       VARCHAR(2)    DEFAULT 'C'             COMMENT 'ABC 分类：A 重点管理，B 常规管理，C 简化管理',
  `status`         TINYINT       DEFAULT 1               COMMENT '状态：1 启用，0 停用',
  `remark`         VARCHAR(255)  DEFAULT NULL            COMMENT '备注',
  `create_time`    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT       DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_code` (`material_code`),
  KEY `idx_material_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料档案表';

-- 9. 仓库表
DROP TABLE IF EXISTS `base_warehouse`;
CREATE TABLE `base_warehouse` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `warehouse_code` VARCHAR(50)  NOT NULL                COMMENT '仓库编码',
  `warehouse_name` VARCHAR(50)  NOT NULL                COMMENT '仓库名称',
  `address`        VARCHAR(255) DEFAULT NULL            COMMENT '仓库地址',
  `manager`        VARCHAR(50)  DEFAULT NULL            COMMENT '仓库负责人',
  `phone`          VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
  `status`         TINYINT      DEFAULT 1               COMMENT '状态：1 启用，0 停用',
  `remark`         VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT      DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_warehouse_code` (`warehouse_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='仓库表';

-- 10. 库位表
DROP TABLE IF EXISTS `base_location`;
CREATE TABLE `base_location` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `location_code` VARCHAR(50)  NOT NULL                COMMENT '库位编码',
  `location_name` VARCHAR(50)  NOT NULL                COMMENT '库位名称',
  `warehouse_id`  BIGINT       NOT NULL                COMMENT '所属仓库主键',
  `status`        TINYINT      DEFAULT 1               COMMENT '状态：1 可用，0 停用',
  `remark`        VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT      DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_location_warehouse` (`warehouse_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库位表';

-- 11. 供应商表
DROP TABLE IF EXISTS `base_supplier`;
CREATE TABLE `base_supplier` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `supplier_code`  VARCHAR(50)  NOT NULL                COMMENT '供应商编码',
  `supplier_name`  VARCHAR(100) NOT NULL                COMMENT '供应商名称',
  `contact_person` VARCHAR(50)  DEFAULT NULL            COMMENT '联系人',
  `phone`          VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
  `address`        VARCHAR(255) DEFAULT NULL            COMMENT '地址',
  `email`          VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
  `status`         TINYINT      DEFAULT 1               COMMENT '状态：1 合作中，0 已停用',
  `remark`         VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT      DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';

-- 12. 客户表
DROP TABLE IF EXISTS `base_customer`;
CREATE TABLE `base_customer` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `customer_code`  VARCHAR(50)  NOT NULL                COMMENT '客户编码',
  `customer_name`  VARCHAR(100) NOT NULL                COMMENT '客户名称',
  `contact_person` VARCHAR(50)  DEFAULT NULL            COMMENT '联系人',
  `phone`          VARCHAR(20)  DEFAULT NULL            COMMENT '联系电话',
  `address`        VARCHAR(255) DEFAULT NULL            COMMENT '地址',
  `status`         TINYINT      DEFAULT 1               COMMENT '状态：1 合作中，0 已停用',
  `remark`         VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT      DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_customer_code` (`customer_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- =====================================================================
-- 三、库存业务表
-- =====================================================================

-- 13. 库存表（按 仓库 + 物料 + 批次 维度记录）
DROP TABLE IF EXISTS `biz_stock`;
CREATE TABLE `biz_stock` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `warehouse_id`  BIGINT        NOT NULL                COMMENT '仓库主键',
  `material_id`   BIGINT        NOT NULL                COMMENT '物料主键',
  `batch_no`      VARCHAR(50)   DEFAULT 'DEFAULT'       COMMENT '批次号',
  `quantity`      DECIMAL(18,2) DEFAULT 0               COMMENT '当前库存数量',
  `locked_qty`    DECIMAL(18,2) DEFAULT 0               COMMENT '锁定数量（已被单据占用但未出库）',
  `avg_cost`      DECIMAL(18,2) DEFAULT 0               COMMENT '移动加权平均成本单价',
  `expire_date`   DATE          DEFAULT NULL            COMMENT '有效期截止日期',
  `last_in_time`  DATETIME      DEFAULT NULL            COMMENT '最近入库时间',
  `last_out_time` DATETIME      DEFAULT NULL            COMMENT '最近出库时间',
  `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT       DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stock` (`warehouse_id`, `material_id`, `batch_no`),
  KEY `idx_stock_material` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

-- 14. 库存流水表
DROP TABLE IF EXISTS `biz_stock_flow`;
CREATE TABLE `biz_stock_flow` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `flow_no`      VARCHAR(50)   DEFAULT NULL            COMMENT '流水号',
  `warehouse_id` BIGINT        NOT NULL                COMMENT '仓库主键',
  `material_id`  BIGINT        NOT NULL                COMMENT '物料主键',
  `flow_type`    TINYINT       NOT NULL                COMMENT '流水类型：1 入库，2 出库，3 调拨入，4 调拨出，5 盘盈，6 盘亏',
  `change_qty`   DECIMAL(18,2) NOT NULL                COMMENT '变动数量（正数增加，负数减少）',
  `before_qty`   DECIMAL(18,2) DEFAULT 0               COMMENT '变动前库存',
  `after_qty`    DECIMAL(18,2) DEFAULT 0               COMMENT '变动后库存',
  `batch_no`     VARCHAR(50)   DEFAULT NULL            COMMENT '批次号',
  `ref_no`       VARCHAR(50)   DEFAULT NULL            COMMENT '关联单据编号',
  `ref_id`       BIGINT        DEFAULT NULL            COMMENT '关联单据主键',
  `operator`     VARCHAR(50)   DEFAULT NULL            COMMENT '操作人',
  `remark`       VARCHAR(255)  DEFAULT NULL            COMMENT '备注',
  `create_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT       DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_flow_material` (`material_id`),
  KEY `idx_flow_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- 15. 入库单主表
DROP TABLE IF EXISTS `biz_inbound`;
CREATE TABLE `biz_inbound` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `inbound_no`    VARCHAR(50)   NOT NULL                COMMENT '入库单号',
  `inbound_type`  TINYINT       NOT NULL DEFAULT 1      COMMENT '入库类型：1 采购入库，2 退货入库，3 其他入库',
  `warehouse_id`  BIGINT        NOT NULL                COMMENT '入库仓库主键',
  `supplier_id`   BIGINT        DEFAULT NULL            COMMENT '供应商主键',
  `handler_id`    BIGINT        DEFAULT NULL            COMMENT '制单人主键',
  `handler_name`  VARCHAR(50)   DEFAULT NULL            COMMENT '制单人姓名',
  `total_amount`  DECIMAL(18,2) DEFAULT 0               COMMENT '入库总金额',
  `status`        TINYINT       DEFAULT 0               COMMENT '单据状态：0 草稿，1 待审核，2 已入库，3 已驳回',
  `audit_user`    VARCHAR(50)   DEFAULT NULL            COMMENT '审核人',
  `audit_time`    DATETIME      DEFAULT NULL            COMMENT '审核时间',
  `audit_remark`  VARCHAR(255)  DEFAULT NULL            COMMENT '审核意见',
  `inbound_time`  DATETIME      DEFAULT NULL            COMMENT '实际入库时间',
  `remark`        VARCHAR(255)  DEFAULT NULL            COMMENT '备注',
  `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT       DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inbound_no` (`inbound_no`),
  KEY `idx_inbound_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单主表';

-- 16. 入库单明细表
DROP TABLE IF EXISTS `biz_inbound_item`;
CREATE TABLE `biz_inbound_item` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `inbound_id`   BIGINT        NOT NULL                COMMENT '入库单主键',
  `material_id`  BIGINT        NOT NULL                COMMENT '物料主键',
  `quantity`     DECIMAL(18,2) NOT NULL                COMMENT '入库数量',
  `unit_price`   DECIMAL(18,2) DEFAULT 0               COMMENT '入库单价',
  `amount`       DECIMAL(18,2) DEFAULT 0               COMMENT '金额小计',
  `batch_no`     VARCHAR(50)   DEFAULT 'DEFAULT'       COMMENT '批次号',
  `produce_date` DATE          DEFAULT NULL            COMMENT '生产日期',
  `expire_date`  DATE          DEFAULT NULL            COMMENT '有效期截止日期',
  `remark`       VARCHAR(255)  DEFAULT NULL            COMMENT '备注',
  `create_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`      TINYINT       DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_inbound_item_pid` (`inbound_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库单明细表';

-- 17. 出库单主表
DROP TABLE IF EXISTS `biz_outbound`;
CREATE TABLE `biz_outbound` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `outbound_no`   VARCHAR(50)   NOT NULL                COMMENT '出库单号',
  `outbound_type` TINYINT       NOT NULL DEFAULT 1      COMMENT '出库类型：1 销售出库，2 领用出库，3 其他出库',
  `warehouse_id`  BIGINT        NOT NULL                COMMENT '出库仓库主键',
  `customer_id`   BIGINT        DEFAULT NULL            COMMENT '客户主键',
  `handler_id`    BIGINT        DEFAULT NULL            COMMENT '制单人主键',
  `handler_name`  VARCHAR(50)   DEFAULT NULL            COMMENT '制单人姓名',
  `total_amount`  DECIMAL(18,2) DEFAULT 0               COMMENT '出库总金额',
  `status`        TINYINT       DEFAULT 0               COMMENT '单据状态：0 草稿，1 待审核，2 已出库，3 已驳回',
  `audit_user`    VARCHAR(50)   DEFAULT NULL            COMMENT '审核人',
  `audit_time`    DATETIME      DEFAULT NULL            COMMENT '审核时间',
  `audit_remark`  VARCHAR(255)  DEFAULT NULL            COMMENT '审核意见',
  `outbound_time` DATETIME      DEFAULT NULL            COMMENT '实际出库时间',
  `remark`        VARCHAR(255)  DEFAULT NULL            COMMENT '备注',
  `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`       TINYINT       DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbound_no` (`outbound_no`),
  KEY `idx_outbound_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单主表';

-- 18. 出库单明细表
DROP TABLE IF EXISTS `biz_outbound_item`;
CREATE TABLE `biz_outbound_item` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `outbound_id` BIGINT        NOT NULL                COMMENT '出库单主键',
  `material_id` BIGINT        NOT NULL                COMMENT '物料主键',
  `quantity`    DECIMAL(18,2) NOT NULL                COMMENT '出库数量',
  `unit_price`  DECIMAL(18,2) DEFAULT 0               COMMENT '出库单价',
  `amount`      DECIMAL(18,2) DEFAULT 0               COMMENT '金额小计',
  `batch_no`    VARCHAR(50)   DEFAULT 'DEFAULT'       COMMENT '批次号',
  `remark`      VARCHAR(255)  DEFAULT NULL            COMMENT '备注',
  `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT       DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_outbound_item_pid` (`outbound_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库单明细表';

-- 19. 调拨单主表
DROP TABLE IF EXISTS `biz_transfer`;
CREATE TABLE `biz_transfer` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `transfer_no`       VARCHAR(50)  NOT NULL                COMMENT '调拨单号',
  `from_warehouse_id` BIGINT       NOT NULL                COMMENT '调出仓库主键',
  `to_warehouse_id`   BIGINT       NOT NULL                COMMENT '调入仓库主键',
  `status`            TINYINT      DEFAULT 0               COMMENT '单据状态：0 草稿，1 待审核，2 已完成，3 已驳回',
  `handler_id`        BIGINT       DEFAULT NULL            COMMENT '制单人主键',
  `handler_name`      VARCHAR(50)  DEFAULT NULL            COMMENT '制单人姓名',
  `audit_user`        VARCHAR(50)  DEFAULT NULL            COMMENT '审核人',
  `audit_time`        DATETIME     DEFAULT NULL            COMMENT '审核时间',
  `transfer_time`     DATETIME     DEFAULT NULL            COMMENT '调拨完成时间',
  `remark`            VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`           TINYINT      DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_transfer_no` (`transfer_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调拨单主表';

-- 20. 调拨单明细表
DROP TABLE IF EXISTS `biz_transfer_item`;
CREATE TABLE `biz_transfer_item` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `transfer_id` BIGINT        NOT NULL                COMMENT '调拨单主键',
  `material_id` BIGINT        NOT NULL                COMMENT '物料主键',
  `quantity`    DECIMAL(18,2) NOT NULL                COMMENT '调拨数量',
  `batch_no`    VARCHAR(50)   DEFAULT 'DEFAULT'       COMMENT '批次号',
  `remark`      VARCHAR(255)  DEFAULT NULL            COMMENT '备注',
  `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`     TINYINT       DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_transfer_item_pid` (`transfer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调拨单明细表';

-- 21. 盘点单主表
DROP TABLE IF EXISTS `biz_stocktaking`;
CREATE TABLE `biz_stocktaking` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `stocktaking_no`   VARCHAR(50)  NOT NULL                COMMENT '盘点单号',
  `warehouse_id`     BIGINT       NOT NULL                COMMENT '盘点仓库主键',
  `status`           TINYINT      DEFAULT 0               COMMENT '状态：0 盘点中，1 已完成',
  `handler_name`     VARCHAR(50)  DEFAULT NULL            COMMENT '盘点人',
  `profit_qty`       DECIMAL(18,2) DEFAULT 0              COMMENT '盘盈总数',
  `loss_qty`         DECIMAL(18,2) DEFAULT 0              COMMENT '盘亏总数',
  `stocktaking_time` DATETIME     DEFAULT NULL            COMMENT '盘点完成时间',
  `remark`           VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `create_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`          TINYINT      DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stocktaking_no` (`stocktaking_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单主表';

-- 22. 盘点单明细表
DROP TABLE IF EXISTS `biz_stocktaking_item`;
CREATE TABLE `biz_stocktaking_item` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `stocktaking_id` BIGINT        NOT NULL                COMMENT '盘点单主键',
  `material_id`    BIGINT        NOT NULL                COMMENT '物料主键',
  `batch_no`       VARCHAR(50)   DEFAULT 'DEFAULT'       COMMENT '批次号',
  `book_qty`       DECIMAL(18,2) DEFAULT 0               COMMENT '账面数量',
  `actual_qty`     DECIMAL(18,2) DEFAULT 0               COMMENT '实盘数量',
  `diff_qty`       DECIMAL(18,2) DEFAULT 0               COMMENT '盈亏数量（正数盘盈，负数盘亏）',
  `remark`         VARCHAR(255)  DEFAULT NULL            COMMENT '备注',
  `create_time`    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT       DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_stocktaking_item_pid` (`stocktaking_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='盘点单明细表';

-- 23. 补货计划表
DROP TABLE IF EXISTS `biz_replenishment`;
CREATE TABLE `biz_replenishment` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `replenish_no`    VARCHAR(50)   NOT NULL                COMMENT '补货计划编号',
  `material_id`     BIGINT        NOT NULL                COMMENT '物料主键',
  `warehouse_id`    BIGINT        NOT NULL                COMMENT '仓库主键',
  `current_qty`     DECIMAL(18,2) DEFAULT 0               COMMENT '当前库存数量',
  `safety_stock`    DECIMAL(18,2) DEFAULT 0               COMMENT '安全库存',
  `suggest_qty`     DECIMAL(18,2) DEFAULT 0               COMMENT '建议补货数量',
  `eoq_qty`         DECIMAL(18,2) DEFAULT 0               COMMENT '经济订货批量（EOQ）',
  `avg_daily_usage` DECIMAL(18,2) DEFAULT 0               COMMENT '平均日消耗量',
  `lead_time`       INT           DEFAULT 3               COMMENT '补货提前期（天）',
  `urgency`         TINYINT       DEFAULT 2               COMMENT '紧急程度：1 紧急，2 一般，3 低',
  `status`          TINYINT       DEFAULT 0               COMMENT '状态：0 待处理，1 已生成采购，2 已忽略',
  `remark`          VARCHAR(255)  DEFAULT NULL            COMMENT '备注',
  `create_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`         TINYINT       DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_replenish_no` (`replenish_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补货计划表';

-- 24. 库存预警记录表
DROP TABLE IF EXISTS `biz_stock_warning`;
CREATE TABLE `biz_stock_warning` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `warehouse_id`   BIGINT       NOT NULL                COMMENT '仓库主键',
  `material_id`    BIGINT       NOT NULL                COMMENT '物料主键',
  `warning_type`   TINYINT      NOT NULL                COMMENT '预警类型：1 低于安全库存，2 呆滞物料，3 临近保质期',
  `warning_level`  TINYINT      DEFAULT 2               COMMENT '预警级别：1 严重，2 警告，3 提示',
  `current_qty`    DECIMAL(18,2) DEFAULT 0              COMMENT '当前库存数量',
  `threshold_value` DECIMAL(18,2) DEFAULT 0             COMMENT '触发阈值',
  `warning_msg`    VARCHAR(255) DEFAULT NULL            COMMENT '预警说明',
  `handled`        TINYINT      DEFAULT 0               COMMENT '处理状态：0 未处理，1 已处理',
  `handle_remark`  VARCHAR(255) DEFAULT NULL            COMMENT '处理说明',
  `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`        TINYINT      DEFAULT 0               COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`),
  KEY `idx_warning_material` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存预警记录表';

-- =====================================================================
-- 四、初始化数据
-- =====================================================================

-- 角色
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`) VALUES
(1, 'ADMIN', '系统管理员', '负责基础数据、用户权限与系统配置'),
(2, 'MANAGER', '仓库主管', '负责单据审核、补货审批与报表分析'),
(3, 'KEEPER', '仓管员', '负责出入库、调拨、盘点等日常作业');

-- 用户（初始密码均为 123456，使用 BCrypt 加密存储）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `gender`, `status`, `remark`) VALUES
(1, 'admin',   '$2a$10$eJh1S2XD/ZBCAOvpv1Rsh.zYSvCBHenALqTg8tMaFnZwRXYk2L9h.', '系统管理员', '13800000001', 'admin@warehouse.com',   1, 1, '系统内置管理员'),
(2, 'manager', '$2a$10$eJh1S2XD/ZBCAOvpv1Rsh.zYSvCBHenALqTg8tMaFnZwRXYk2L9h.', '王主管',     '13800000002', 'manager@warehouse.com', 1, 1, '仓库主管'),
(3, 'keeper',  '$2a$10$eJh1S2XD/ZBCAOvpv1Rsh.zYSvCBHenALqTg8tMaFnZwRXYk2L9h.', '李仓管',     '13800000003', 'keeper@warehouse.com',  2, 1, '一线仓管员');

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1), (2, 2), (3, 3);

-- 计量单位
INSERT INTO `base_unit` (`id`, `unit_name`, `unit_code`) VALUES
(1, '个', 'PCS'), (2, '箱', 'BOX'), (3, '千克', 'KG'),
(4, '米', 'M'), (5, '台', 'SET'), (6, '瓶', 'BTL');

-- 物料分类
INSERT INTO `base_category` (`id`, `category_code`, `category_name`, `parent_id`, `sort_no`, `remark`) VALUES
(1, 'C01', '办公用品', 0, 1, '日常办公耗材'),
(2, 'C02', '电子设备', 0, 2, 'IT 类设备与配件'),
(3, 'C03', '生产原料', 0, 3, '生产用原材料'),
(4, 'C0101', '纸张耗材', 1, 1, '打印纸、便签等'),
(5, 'C0102', '书写工具', 1, 2, '笔、记号笔等');

-- 仓库
INSERT INTO `base_warehouse` (`id`, `warehouse_code`, `warehouse_name`, `address`, `manager`, `phone`, `status`, `remark`) VALUES
(1, 'WH01', '中心仓库', '厂区 A 座 1 层', '王主管', '13800000002', 1, '主仓，存放常用物料'),
(2, 'WH02', '备用仓库', '厂区 B 座负一层', '李仓管', '13800000003', 1, '存放低频与备货物料');

-- 库位
INSERT INTO `base_location` (`id`, `location_code`, `location_name`, `warehouse_id`, `status`, `remark`) VALUES
(1, 'WH01-A-01', 'A 区 01 货位', 1, 1, '重货区'),
(2, 'WH01-A-02', 'A 区 02 货位', 1, 1, '重货区'),
(3, 'WH01-B-01', 'B 区 01 货位', 1, 1, '轻货区'),
(4, 'WH02-A-01', 'A 区 01 货位', 2, 1, '备货区');

-- 供应商
INSERT INTO `base_supplier` (`id`, `supplier_code`, `supplier_name`, `contact_person`, `phone`, `address`, `email`, `status`, `remark`) VALUES
(1, 'SUP01', '晨光文具有限公司', '张经理', '13900000001', '上海市浦东新区示例路 100 号', 'cg@example.com', 1, '办公用品主供应商'),
(2, 'SUP02', '华兴电子科技有限公司', '刘经理', '13900000002', '深圳市南山区科技园示例大厦', 'hx@example.com', 1, '电子设备供应商'),
(3, 'SUP03', '中原原料贸易有限公司', '赵经理', '13900000003', '郑州市高新区示例大道 8 号', 'zy@example.com', 1, '生产原料供应商');

-- 客户
INSERT INTO `base_customer` (`id`, `customer_code`, `customer_name`, `contact_person`, `phone`, `address`, `status`, `remark`) VALUES
(1, 'CUS01', '第一分公司', '陈经理', '13700000001', '北京市朝阳区示例街 1 号', 1, '长期合作客户'),
(2, 'CUS02', '第二分公司', '孙经理', '13700000002', '广州市天河区示例路 2 号', 1, '长期合作客户'),
(3, 'CUS03', '零售客户', '周先生', '13700000003', '本地零售门店', 1, '零售渠道');

-- 物料档案（含 ABC 分类与安全库存，用于补货与预警演示）
INSERT INTO `base_material` (`id`, `material_code`, `material_name`, `category_id`, `unit_id`, `spec`, `model`, `brand`, `safety_stock`, `max_stock`, `purchase_price`, `sale_price`, `shelf_life_days`, `abc_type`, `status`, `remark`) VALUES
(1,  'M0001', 'A4 复印纸',      4, 2, '70g 500 张/包', 'A4-70G',  '晨光',   50,  500, 18.50,  25.00, 0,   'A', 1, '高频消耗品，重点管理'),
(2,  'M0002', '中性签字笔',      5, 1, '0.5mm 黑色',    'AGP-01',  '晨光',  100, 1000,  2.50,   4.00, 0,   'A', 1, '高频消耗品'),
(3,  'M0003', '便签纸',          4, 1, '76×76mm',       'BS-76',   '得力',   80,  600,  3.20,   5.00, 0,   'B', 1, '常用耗材'),
(4,  'M0004', '硒鼓',            2, 1, '兼容 CF280A',    'CF280A',  '惠普',   10,  100, 320.00, 450.00, 730, 'A', 1, '单价值高，需重点管理'),
(5,  'M0005', 'USB 键盘',        2, 5, '有线 104 键',    'K-104',   '罗技',   15,  150,  95.00, 150.00, 0,   'B', 1, 'IT 配件'),
(6,  'M0006', '无线鼠标',        2, 1, '2.4G 静音',      'M-220',   '罗技',   20,  200,  68.00, 110.00, 0,   'A', 1, 'IT 配件'),
(7,  'M0007', '标记笔',          5, 1, '黑色 双头',      'MK-01',   '晨光',   60,  500,  4.00,   6.50, 0,   'C', 1, '低频耗材'),
(8,  'M0008', '工业酒精',        3, 6, '500ml/瓶',       'AL-500',  '中原',   30,  300, 12.00,  18.00, 365, 'B', 1, '有保质期，需临期预警'),
(9,  'M0009', '防静电手套',      3, 1, '均码 100 只/包', 'GLOVE-100', '华兴', 40,  400, 22.00,  33.00, 0,   'C', 1, '劳保用品'),
(10, 'M0010', '网线',            2, 4, 'CAT6 无氧铜',    'CAT6-305', '华兴',  200, 2000, 2.80,   4.50, 0,   'B', 1, '按米管理');

-- 初始库存（部分物料故意低于安全库存，用于演示补货建议与预警）
INSERT INTO `biz_stock` (`id`, `warehouse_id`, `material_id`, `batch_no`, `quantity`, `locked_qty`, `avg_cost`, `expire_date`, `last_in_time`) VALUES
(1,  1, 1,  'DEFAULT', 320.00, 0, 18.50, NULL, '2026-09-01 09:00:00'),
(2,  1, 2,  'DEFAULT', 60.00,  0,  2.50, NULL, '2026-09-05 10:00:00'),
(3,  1, 3,  'DEFAULT', 210.00, 0,  3.20, NULL, '2026-09-06 11:00:00'),
(4,  1, 4,  'B20260601', 6.00, 0, 320.00, '2028-06-01', '2026-06-01 09:30:00'),
(5,  1, 5,  'DEFAULT', 45.00,  0, 95.00, NULL, '2026-08-20 14:00:00'),
(6,  1, 6,  'DEFAULT', 12.00,  0, 68.00, NULL, '2026-09-10 15:00:00'),
(7,  1, 7,  'DEFAULT', 260.00, 0,  4.00, NULL, '2026-07-15 09:00:00'),
(8,  1, 8,  'B20251015', 25.00, 0, 12.00, '2026-10-15', '2025-10-15 10:00:00'),
(9,  2, 9,  'DEFAULT', 120.00, 0, 22.00, NULL, '2026-08-01 09:00:00'),
(10, 2, 10, 'DEFAULT', 850.00, 0,  2.80, NULL, '2026-08-10 09:00:00');

-- 入库单（含已入库与待审核两种状态）
INSERT INTO `biz_inbound` (`id`, `inbound_no`, `inbound_type`, `warehouse_id`, `supplier_id`, `handler_id`, `handler_name`, `total_amount`, `status`, `audit_user`, `audit_time`, `inbound_time`, `remark`) VALUES
(1, 'IN20260901001', 1, 1, 1, 3, '李仓管', 3700.00, 2, '王主管', '2026-09-01 10:00:00', '2026-09-01 09:00:00', '月初办公用品补货'),
(2, 'IN20260905002', 1, 1, 3, 3, '李仓管',  600.00, 1, NULL, NULL, NULL, '工业酒精补货，待审核'),
(3, 'IN20260810003', 1, 2, 2, 3, '李仓管',  952.00, 2, '王主管', '2026-08-10 10:00:00', '2026-08-10 09:00:00', '网络与劳保物资入库');

INSERT INTO `biz_inbound_item` (`inbound_id`, `material_id`, `quantity`, `unit_price`, `amount`, `batch_no`, `produce_date`, `expire_date`, `remark`) VALUES
(1, 1, 200.00, 18.50, 3700.00, 'DEFAULT', NULL, NULL, 'A4 纸整箱入库'),
(2, 8,  50.00, 12.00,  600.00, 'B20260905', '2026-09-05', '2027-09-05', '酒精补货'),
(3, 9, 100.00, 22.00, 2200.00, 'DEFAULT', NULL, NULL, '劳保手套');

-- 出库单
INSERT INTO `biz_outbound` (`id`, `outbound_no`, `outbound_type`, `warehouse_id`, `customer_id`, `handler_id`, `handler_name`, `total_amount`, `status`, `audit_user`, `audit_time`, `outbound_time`, `remark`) VALUES
(1, 'OUT20260910001', 2, 1, NULL, 3, '李仓管', 1250.00, 2, '王主管', '2026-09-10 14:00:00', '2026-09-10 15:00:00', '行政部领用办公用品'),
(2, 'OUT20260911002', 1, 1, 1, 3, '李仓管',  900.00, 1, NULL, NULL, NULL, '北京分公司订单，待审核');

INSERT INTO `biz_outbound_item` (`outbound_id`, `material_id`, `quantity`, `unit_price`, `amount`, `batch_no`, `remark`) VALUES
(1, 1, 50.00, 25.00, 1250.00, 'DEFAULT', '行政部领用'),
(2, 4,  2.00, 450.00, 900.00, 'B20260601', '分公司硒鼓');

-- 库存流水（与已完成的出入库单据对应）
INSERT INTO `biz_stock_flow` (`flow_no`, `warehouse_id`, `material_id`, `flow_type`, `change_qty`, `before_qty`, `after_qty`, `batch_no`, `ref_no`, `ref_id`, `operator`, `remark`) VALUES
('FL20260901001', 1, 1, 1, 200.00, 170.00, 370.00, 'DEFAULT',    'IN20260901001',  1, '李仓管', '采购入库'),
('FL20260910002', 1, 1, 2, -50.00, 370.00, 320.00, 'DEFAULT',    'OUT20260910001', 1, '李仓管', '领用出库'),
('FL20260810003', 2, 9, 1, 100.00,  20.00, 120.00, 'DEFAULT',    'IN20260810003',  3, '李仓管', '采购入库');

-- 公告
INSERT INTO `sys_notice` (`title`, `content`, `publisher`, `status`, `publish_time`) VALUES
('系统试运行通知', '仓库库存管理系统即日起进入试运行阶段，请各岗位按流程录入单据。', '系统管理员', 1, '2026-09-01 08:30:00'),
('月度盘点安排', '本月 28 日进行全仓盘点，请提前处理未审核单据。', '王主管', 1, '2026-09-20 09:00:00');

-- =====================================================================
-- 脚本结束
-- =====================================================================
