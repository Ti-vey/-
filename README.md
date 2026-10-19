# 仓库库存管理系统

本科毕业设计项目：**基于 Spring Boot 与 Vue 的仓库库存管理系统设计与实现——融合 ABC 分类与智能补货建议**。

## 一、技术栈

| 层次 | 技术选型 | 说明 |
| --- | --- | --- |
| 后端 | Spring Boot 2.7.18 + MyBatis-Plus 3.5.3.1 | 本机 JDK 为 11，Spring Boot 3.x 要求 JDK 17，因此选择 2.7.x |
| 数据库 | MySQL 5.7 + 连接池 HikariCP | 兼容 5.7，未使用 8.0 专有语法 |
| 前端 | Vue 3 + Vite 5 + Element Plus + Pinia + Vue Router 4 | 组合式 API（script setup） |
| 图表 | ECharts 5 | 首页看板与统计分析 |
| 认证 | JWT（jjwt 0.11.5） | 无状态登录认证 |
| 其他 | Lombok、Hutool（BCrypt 密码加密）、Axios | — |

## 二、目录结构

```
project/
├── backend/                       # Spring Boot 后端工程
│   └── src/main/java/com/warehouse/
│       ├── common/                # 统一返回、异常处理、实体基类
│       ├── config/                # MyBatis-Plus、跨域、拦截器注册、字段自动填充
│       ├── security/              # JWT 工具、登录拦截器、用户上下文
│       ├── entity/                # 24 个实体类，与数据库表一一对应
│       ├── mapper/                # MyBatis-Plus Mapper
│       ├── service/ + impl/       # 业务接口与实现（核心业务逻辑集中在此）
│       ├── controller/            # RESTful 接口
│       ├── dto/ + vo/             # 请求参数对象与响应对象
│       └── util/                  # 单据编号生成工具
├── frontend/                      # Vue3 前端工程
│   └── src/
│       ├── api/                   # 接口封装（统一收口）
│       ├── components/            # CrudTable（通用增删改查）、OrderPage（单据主子表）
│       ├── layout/                # 主框架布局
│       ├── router/                # 路由与登录守卫
│       ├── stores/                # Pinia 用户状态
│       ├── utils/                 # Axios 封装
│       └── views/                 # 各功能页面
└── docs/
    └── 04_数据库设计.sql          # 建库建表脚本 + 初始化数据
```

## 三、环境要求

- JDK 11（本机已验证 java 11.0.22）
- Maven 3.9+
- Node.js 18+（本机为 v22.14.0）
- MySQL 5.7（建表脚本同时兼容 8.0）

## 四、启动步骤

### 0. 配置数据库连接



1. 把 `backend/src/main/resources/application-local.yml.example` 复制为同目录下的 `application-local.yml`
2. 打开它，填入自己的数据库密码（用户名、地址按需修改）
3. `application-local.yml` 已在 `.gitignore` 中，不会被提交

也可以通过环境变量注入，无需创建该文件：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`JWT_SECRET`。

### 1. 初始化数据库

```bash
mysql -uroot -p你的数据库密码 --default-character-set=utf8mb4 < "docs/04_数据库设计.sql"
```

脚本会创建数据库 `warehouse_db`，包含 **24 张表**与演示数据（10 种物料、2 个仓库、3 个账号等）。

### 2. 启动后端

```bash
cd backend
mvn clean package -DskipTests
java -jar target/warehouse-system-1.0.0.jar
```

启动成功后访问 http://localhost:8080 ，控制台输出「仓库库存管理系统后端启动成功」。

### 3. 启动前端

```bash
cd frontend
npm install        # 首次运行需要
npm run dev
```

浏览器打开 http://localhost:5173 ，开发服务器已把 `/api` 请求代理到后端 8080 端口。

### 4. 演示账号

| 账号 | 密码 | 角色 | 权限范围 |
| --- | --- | --- | --- |
| admin | 123456 | 系统管理员 | 基础数据、用户权限、全部业务 |
| manager | 123456 | 仓库主管 | 单据审核、补货审批、报表分析 |
| keeper | 123456 | 仓管员 | 出入库、调拨、盘点等日常作业 |

## 五、功能模块

1. **基础数据**：物料分类、物料档案、计量单位、仓库、库位、供应商、客户
2. **入库管理**：采购/退货/其他入库单的录入、明细维护、提交审核、审核过账、**单据打印**
3. **出库管理**：销售/领用/其他出库单，审核前校验库存，防止负库存，**单据打印**
4. **库存调拨**：仓库间转移，一出一入同事务完成
5. **库存盘点**：按账面库存生成盘点单，录入实盘数量，盈亏自动调整库存
6. **库存查询**：支持「库存明细 / 按物料汇总 / 按仓库汇总」三个维度切换，配合库存流水台账
7. **智能补货**：ABC 分类 + 安全库存模型 + EOQ 经济订货批量
8. **库存预警**：低于安全库存、呆滞物料、临近保质期三类预警
9. **统计分析**：首页看板（含**低库存物料提醒**与**临期批次提醒**两张待办清单）、出入库趋势、ABC 帕累托分析、库存周转率、仓库金额分布
10. **系统管理**：用户与角色管理、操作日志、公告

## 六、创新点与代码位置

| 创新点 | 说明 | 核心代码 |
| --- | --- | --- |
| 安全库存 + EOQ 智能补货 | 近 30 天平均日耗推导安全库存，EOQ 公式计算经济订货批量 | `service/impl/BizReplenishmentServiceImpl.java` |
| ABC 分类分析 | 按库存金额降序计算累计占比，按 70%/90% 划分 A/B/C 类 | `BizStockServiceImpl.abcAnalysis()` |
| 库存周转与呆滞识别 | 按统计周期计算周转率，识别零出库的呆滞物料 | `BizStockServiceImpl.turnoverAnalysis()` |
| 并发防超卖 | 扣减库存使用条件更新（quantity >= 扣减量），失败即回滚 | `BizStockServiceImpl.decreaseStock()` |
| 移动加权平均成本 | 入库时按「原有库存金额 + 本次入库金额 ÷ 入库后总数量」更新成本 | `BizStockServiceImpl.increaseStock()` |
| 单据状态机 | 草稿 → 待审核 → 已过账/已驳回，非法流转直接拦截 | 各单据 XxxServiceImpl.audit() |


## 七、同类系统对比与设计借鉴


**本系统优秀的地方**

| 单据审核 | 仅暂存/作废/完成，无审核环节 | 草稿→待审核→已过账/已驳回，具备审批控制 |
| 批次与效期 | lite 分支无批次与效期管理 | 库存按批次维度记录，支持有效期与临期预警 |
| 并发控制 | `selectOne` 后批量更新，先查后改存在超卖风险 | 扣减走 `quantity >= 扣减量` 条件更新，失败即回滚 |
| 补货决策 | 无，仅有简单库存预警 | ABC 分类 + 安全库存模型 + EOQ 经济订货批量 |
| 成本核算 | 库存只记录数量，无成本 | 入库按移动加权平均法维护成本单价 |
