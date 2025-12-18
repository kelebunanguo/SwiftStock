## SwiftStock 电商仓库管理系统

SwiftStock 是一个基于 **Spring Boot 3 + Vue3 + MySQL** 的电商仓库管理系统，主要用于展示从需求分析、架构设计、数据库建模到前后端实现的完整过程，可直接作为毕业设计/项目作品推到 GitHub。

系统围绕「**商品 - 订单 - 库存**」三大核心对象，支持商品与分类管理、订单全流程管理、库存出入库与记录、库存预警以及仪表盘与报表统计。

---

### 1. 功能概览

- **登录与基础信息**
  - 管理员登录（演示版：固定账号 `admin/admin`）
  - 获取当前用户信息、退出登录

- **商品与分类管理**
  - 商品新增 / 编辑 / 删除 / 列表查询（按名称、分类筛选）
  - 商品编码自动生成（触发器：`P00001` 这种格式）
  - 分类多级管理（父子分类、男装/女装/外套等）

- **订单管理**
  - 创建订单（支持一次选择多件商品）
  - 订单列表、条件查询、详情查看
  - 订单状态机：UNPAID → PAID → PREPARING → SHIPPED → DELIVERED → COMPLETED
  - 取消 / 退款等异常分支（CANCELLED、REFUNDED）
  - 订单状态历史表 `order_status_history` 记录每次状态变更及原因

- **库存管理**
  - 库存列表：按商品名、分类、库存状态筛选
  - 入库 / 出库操作
  - 库存变动记录 `inventory_record`（数量、原因、时间等）
  - 批量库存操作（采购、退货、调拨、损耗等）

- **库存预警与仪表盘**
  - 安全库存阈值 `min_stock_level` 设置
  - 低库存 / 缺货商品预警
  - 仪表盘统计：商品数、订单数、库存总量、预警数、最近订单

- **报表与销售分析**
  - 库存报表：正常 / 低库存 / 缺货统计、库存总价值
  - 销售趋势：按日聚合销售额折线图
  - 销售概览：总销售额、今日销售额、本月销售额、订单数量

---

### 2. 技术栈

- **前端**
  - Vue 3 + Vite
  - Element Plus
  - Axios
  - ECharts
  - vue-router（路由与简单鉴权）

- **后端**
  - Spring Boot **3.4.1**
  - Spring Web
  - Spring Security（只做简单放行规则，登录逻辑为演示版）
  - MyBatis + MyBatis-Spring-Boot-Starter **3.0.4**
  - SpringDoc OpenAPI **2.7.0**（Swagger UI）

- **数据库**
  - MySQL 8.0
  - 主库脚本：`ecommerce_warehouse.sql`

- **其他**
  - Maven
  - Lombok
  - Logback 日志（`logback-spring.xml`）

---

### 3. 项目结构

仓库根目录结构（关键部分）：

```text
SwiftStock/
├─ pom.xml                      # 后端 Maven 配置（Spring Boot 3.4.1）
├─ src/
│  └─ main/
│     ├─ java/com/swiftstock/
│     │  ├─ SwiftStockApplication.java       # 启动类
│     │  ├─ config/                          # 配置类
│     │  │  ├─ SecurityConfig.java           # Spring Security 放行规则
│     │  │  ├─ CorsConfig.java               # 全局跨域配置
│     │  │  ├─ GlobalExceptionHandler.java   # 全局异常处理 (统一返回 JSON)
│     │  │  ├─ OpenApiConfig.java            # Swagger/OpenAPI 配置
│     │  │  └─ OrderStatusTypeHandler.java   # 订单状态枚举 TypeHandler
│     │  ├─ controller/                      # REST 接口层
│     │  ├─ service/                         # 业务逻辑层 (impl/*)
│     │  ├─ mapper/                          # MyBatis Mapper 接口
│     │  └─ entity/                          # 实体类 (Product/Order/OrderStatusHistory/...)
│     └─ resources/
│        ├─ application.yml                  # 应用配置 (端口、数据源、mybatis等)
│        ├─ logback-spring.xml              # 日志配置
│        └─ mapper/                          # MyBatis XML 映射
│           ├─ ProductMapper.xml
│           ├─ OrderMapper.xml
│           ├─ OrderStatusHistoryMapper.xml
│           └─ ...
│
├─ frontend/                                 # Vue3 前端工程
│  ├─ package.json
│  ├─ vite.config.js
│  └─ src/
│     ├─ main.js
│     ├─ api/index.js                        # Axios API 封装
│     ├─ router/index.js                     # 路由与登录守卫
│     ├─ views/
│     │  ├─ Dashboard.vue
│     │  ├─ Login.vue
│     │  ├─ product/...
│     │  ├─ order/...
│     │  ├─ inventory/...
│     │  └─ report/...
│     └─ components/
│        ├─ AppLayout.vue
│        ├─ AppHeader.vue
│        ├─ AppSidebar.vue
│        └─ StockAlertWidget.vue
│
└─ ecommerce_warehouse.sql                   # 初始建库+示例数据脚本

```

---

### 4. 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+（推荐） 和 npm
- MySQL 8.0+

---

### 5. 数据库准备



如果你还没有数据库，可以直接使用仓库根目录下的 `ecommerce_warehouse.sql`：

```bash
mysql -u root -p < ecommerce_warehouse.sql
```

该脚本会：
- 创建数据库 `ecommerce_warehouse`
- 创建所有表结构（admin/category/product/orders/...）
- 插入一份示例数据



执行完后，仪表盘、订单管理、销售趋势图都能展示较为丰富的数据，适合演示和截图写论文。

---

### 6. 后端启动步骤（Spring Boot）

1. **导入项目**
   - 使用 IntelliJ IDEA / Eclipse 导入 Maven 项目

2. **确认配置**
   - 编辑 `src/main/resources/application.yml` 中的数据源配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce_warehouse?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456   # 按你的本地 MySQL 修改
```

3. **使用 Maven 编译**

```bash
mvn clean install
```

4. **启动后端**

```bash
mvn spring-boot:run
# 或在 IDE 中直接运行 SwiftStockApplication.main()
```

5. **访问后端接口**
   - 基础地址（默认）：`http://localhost:9090/swiftstock`
   - Swagger UI：`http://localhost:9090/swiftstock/swagger-ui.html`

---

### 7. 前端启动步骤（Vue3 + Vite）

1. **安装依赖**

```bash
cd frontend
npm install
```

2. **开发模式启动**

```bash
npm run dev
```

默认开发地址一般为：`http://localhost:5173`

> 注：前端通过 Axios 的 `baseURL` 访问后端 `/swiftstock`，如果你修改了端口或 context-path，需要同步调整 `frontend/src/api/index.js`。

3. **构建生产包（可选）**

```bash
npm run build
```

构建结果会输出到 `frontend/dist/`，后端已通过 `application.yml` 将该目录作为静态资源映射：

```yaml
spring:
  web:
    resources:
      static-locations: classpath:/static/,file:./frontend/dist/
```

可以选择：
- 用 Nginx 单独托管前端
- 或将 `dist` 直接由 Spring Boot 静态资源托管

---

### 8. 登录方式（演示版）

当前登录逻辑是为毕业设计/演示简化的版本：

- 登录接口：`POST /swiftstock/auth/login`
- 账号密码：`admin / admin`
- 返回：`{ success, data: { token, username, role } }`
- 前端会把 `token` 存到 `localStorage`，并在路由守卫和 Axios 拦截器中判断是否已登录。



---

### 9. API 文档

项目已集成 SpringDoc OpenAPI，你可以在运行后端后访问：

- Swagger UI：`http://localhost:9090/swiftstock/swagger-ui.html`
- OpenAPI JSON：`http://localhost:9090/swiftstock/api-docs`

主要模块接口包括：

- `/products/**`    商品管理
- `/categories/**`  分类管理
- `/inventory/**`   库存与库存记录
- `/orders/**`      订单与状态流转、状态历史
- `/stock-alert/**` 库存预警
- `/dashboard/**`   仪表盘统计
- `/reports/**`     库存报表
- `/sales/**`       销售趋势与概览
- `/auth/**`        登录/登出/用户信息

---

 本项目为本人本科毕业设计《SwiftStock 电商仓库管理系统的设计与实现》的代码与文档仓库。




