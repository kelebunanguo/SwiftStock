## SwiftStock 电商仓库管理系统

SwiftStock 是一个基于 **Spring Boot 3 + Vue 3 + MySQL** 的电商仓库管理系统，适合作为毕业设计或演示项目。项目覆盖商品、订单、库存、供货与补货推荐（AI）等功能，并提供基于 OpenAPI 的在线接口文档（Swagger UI）。

**在线 API 文档（运行后）**： `http://localhost:9090/swiftstock/swagger-ui`

---

### 1. 功能概览

- **登录与基础信息**
  - 管理员登录（基于 JWT），请在生产环境创建管理员账户或通过迁移脚本导入。
  - 查询当前用户信息、登出接口。

- **商品与分类管理**
  - 商品新增 / 编辑 / 删除 / 列表查询（按名称、分类筛选）
  - 商品编码自动生成（示例格式：`P00001`）
  - 分类支持多级管理（父子分类）

- **订单管理**
  - 创建订单（支持多商品）
  - 订单列表、条件查询、详情查看
  - 订单状态机：UNPAID → PAID → PREPARING → SHIPPED → DELIVERED → COMPLETED
  - 订单状态历史表 `order_status_history` 记录变更

- **库存管理**
  - 库存查询、入库 / 出库操作
  - 库存变动记录 `inventory_record`（数量、原因、时间）
  - 批量库存操作（采购、退货、调拨、损耗）

- **库存预警与仪表盘**
  - 设置安全库存阈值 `min_stock_level`
  - 低库存 / 缺货报警与列表
  - 仪表盘展示关键指标

- **报表与销售分析**
  - 库存报表、销售趋势图、销售概览

- **供货商（Supplier）与供货记录（SupplyRecord）**
  - 管理供应商信息、记录供货历史并联动库存

- **AI 补货推荐模块**
  - 基于历史销量与库存规则生成可操作补货建议

---

### 2. 技术栈

- **前端**
  - Vue 3 + Vite
  - Element Plus、Axios、ECharts

- **后端**
  - Spring Boot 3.4.1
  - Spring Web、Spring Security（JWT）
  - MyBatis + mybatis-spring-boot-starter 3.0.4
  - SpringDoc OpenAPI（springdoc-openapi-starter-webmvc-ui）

- **数据库**
  - MySQL 8.0（初始化脚本：`ecommerce_warehouse.sql`）

- **其他**
  - Maven、Lombok、Logback

---

### 3. 项目结构（关键文件）

```text
SwiftStock/
├─ pom.xml                             # 后端 Maven 配置
├─ src/
│  └─ main/
│     ├─ java/com/swiftstock/
│     │  ├─ SwiftStockApplication.java  # 启动类
│     │  ├─ config/                     # 应用配置类（Security/OpenAPI/异常处理等）
│     │  ├─ controller/                 # REST 控制器（按功能模块分组，列出主要 controller ）
│     │  │  ├─ AuthController.java           # /auth — 登录、登出、当前用户信息
│     │  │  ├─ ProductController.java        # /products — 商品管理（CRUD、分页）
│     │  │  ├─ CategoryController.java       # /categories — 商品分类管理（树/分页/CRUD）
│     │  │  ├─ InventoryController.java      # /inventory — 库存管理（CRUD、列表）
│     │  │  ├─ StockAlertController.java     # /stock-alert — 预警与统计
│     │  │  ├─ OrderController.java          # /orders — 订单管理（CRUD、分页）
│     │  │  ├─ SupplierController.java       # /suppliers — 供应商管理（CRUD、分页）
│     │  │  ├─ SupplyRecordController.java   # /supply-records — 供货记录（按 supplierId 列表CRUD）
│     │  │  ├─ AiForecastController.java     # /ai/forecast — AI 补货接口
│     │  │  ├─ AiProductController.java      # /ai/products — AI 生成商品描述商品接口
│     │  │  ├─ DashboardController.java      # /dashboard — 仪表盘统计与数据聚合
│     │  │  ├─ SalesController.java          # /sales — 销售统计与趋势
│     │  │  └─ ReportController.java         # /reports — 报表统计
│     │  ├─ service/                    # 业务逻辑接口与实现
│     │  ├─ mapper/                     # MyBatis Mapper 接口
│     │  └─ entity/                     # 实体类（Product/Order/...）
│     └─ resources/
│        ├─ application.yml             # 应用配置（端口、context-path、数据源、springdoc）
│        └─ mapper/                     # MyBatis XML 映射文件
└─ frontend/                            # Vue3 前端工程（独立项目目录）
   ├─ package.json
   ├─ vite.config.js
   └─ src/
      ├─ api/index.js                   # Axios 封装（包含 ai/supplyRecord API）
      ├─ views/
      └─ components/
```

---

### 4. 供货商（Supplier）模块 — 设计与接口

目的：管理供应商并记录每次供货，供货记录用于库存入库、采购统计与为 AI 补货策略提供数据输入。

数据模型（示例）
- `supplier`：
  - id: bigint (PK)
  - name: varchar
  - contact_person: varchar
  - phone: varchar
  - email: varchar
  - address: varchar
  - remark: text

- `supply_record`：
  - id: bigint (PK)
  - supplier_id: bigint (FK -> supplier.id)
  - product_id: bigint (FK -> product.id)
  - quantity: int
  - unit_price: decimal
  - total_price: decimal
  - supply_date: datetime
  - created_by: bigint
  - remark: text

主要接口（示例路径）
- 供应商管理
  - GET `/swiftstock/api/suppliers` — 列表（支持 name、page、size）
  - GET `/swiftstock/api/suppliers/{id}` — 详情
  - POST `/swiftstock/api/suppliers` — 新增
  - PUT `/swiftstock/api/suppliers/{id}` — 更新
  - DELETE `/swiftstock/api/suppliers/{id}` — 删除

- 供货记录（SupplyRecord）
  - GET `/swiftstock/api/supply-records` — 列表（支持 supplierId、productId、dateRange、分页）
  - POST `/swiftstock/api/supply-records` — 新增（会触发库存入库）
  - PUT `/swiftstock/api/supply-records/{id}` — 更新（修改库存需谨慎）
  - DELETE `/swiftstock/api/supply-records/{id}` — 删除（并回滚库存变更，需管理员确认）

新增供货记录示例请求体（JSON）

```json
{
  "supplierId": 12,
  "productId": 101,
  "quantity": 500,
  "unitPrice": 3.25,
  "supplyDate": "2025-12-01T10:00:00",
  "remark": "常规补货"
}
```

业务流程建议
- 新增 `supply_record` 时：
  - 验证 supplier 与 product 存在
  - 在事务中创建供货记录并增加库存（写入 `inventory_record`）
  - 记录操作人（created_by）

- 支持按 supplier 聚合统计（近 30 天供货量、供货商品排行）

---

### 5. AI 补货推荐模块 — 设计详述

目标：基于历史销量与库存生成优先级补货建议，支持离线 / 异步计算与前端缓存读取。

核心输入
- 历史销量（按 SKU 按日聚合）
- 当前库存（product.current_stock）
- 最小安全库存（product.min_stock）

核心输出（RecommendItem）
- productId, productName, currentStock, minStock
- forecastSales7Days
- suggestReorderQuantity
- priority（HIGH / MEDIUM / LOW）
- advice（短文本）

算法建议（分层）
1. 规则层（轻量级）
   - 若 currentStock <= minStock → 建议补货至 minStock + safetyBuffer
   - 若 forecastSales7Days > currentStock → 建议补货 forecastSales7Days - currentStock
2. 预测层（可选）
   - 使用简单时间序列（移动平均 / 指数平滑）或 ML（LightGBM）进行 7 天预测
3. 优先级与分批
   - 对 HIGH 优先级商品生成即时采购建议

缓存与部署建议
- 异步计算并写入 Redis，接口读取缓存数据；提供强制刷新 `force=true` 的端点以触发重新计算。
- 缓存键示例：`ai_replenish_cache_v1:{warehouseId}`

示例接口
- GET `/swiftstock/api/ai/forecast/recommend-count`
- GET `/swiftstock/api/ai/forecast/recommend-list?page=1&size=50`
- POST `/swiftstock/api/ai/forecast/compute`（触发重新计算）

示例返回单项

```json
{
  "productId": 101,
  "productName": "示例商品 A",
  "currentStock": 20,
  "minStock": 50,
  "forecastSales7Days": 80,
  "suggestReorderQuantity": 60,
  "priority": "HIGH",
  "advice": "基于7天预测销量和最低库存，建议立即补货60件"
}
```

实现注意
- 推荐计算应异步化，避免在主请求线程阻塞。
- 上线后的模型/规则变更请采用版本控制与回滚策略。

---

### 6. 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+（前端）
- MySQL 8.0+

---

### 7. 数据库准备

使用项目根目录的 `ecommerce_warehouse.sql` 初始化数据库：

```bash
mysql -u root -p < ecommerce_warehouse.sql
```

---

### 8. 后端启动（快速）

1. 编辑 `src/main/resources/application.yml` 中数据库与端口配置
2. 构建并运行：

```bash
mvn clean install
mvn spring-boot:run
```

默认应用根： `http://localhost:9090/swiftstock`
Swagger UI： `http://localhost:9090/swiftstock/swagger-ui`

---

### 9. 前端启动（快速）

```bash
cd frontend
npm install
npm run dev
```

默认开发地址： `http://localhost:5173`

---

### 10. 常用 API（示例）

- `/auth/**` — 登录 / 当前用户
- `/api/products/**` — 商品管理
- `/api/categories/**` — 分类管理
- `/api/orders/**` — 订单管理
- `/api/inventory/**` — 库存与记录
- `/api/suppliers/**` — 供货商管理
- `/api/supply-records/**` — 供货记录
- `/api/ai/forecast/**` — AI 补货接口

---

 本项目为本人本科毕业设计《基于springboot的电商仓库管理系统的设计与实现》的代码与文档仓库。


