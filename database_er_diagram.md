erDiagram
    %% ========== 管理员模块 ==========
    admin {
        bigint id PK "管理员ID"
        varchar username UK "登录用户名"
        varchar password "密码哈希"
        varchar name "姓名"
        varchar email "邮箱"
        varchar phone "联系电话"
        timestamp created_time "创建时间"
        timestamp updated_time "更新时间"
    }

    %% ========== 分类模块（自关联） ==========
    category {
        bigint id PK "分类ID"
        varchar name "分类名称"
        bigint parent_id FK "父级分类ID"
        int level "层级：1-一级，2-二级，3-三级"
        int sort_order "排序值"
        text description "描述"
        timestamp created_time "创建时间"
        timestamp updated_time "更新时间"
    }

    category ||--o{ category : "父分类-子分类"

    %% ========== 供应商模块 ==========
    supplier {
        bigint id PK "供应商ID"
        varchar name UK "供应商名称"
        varchar contact_person "联系人"
        varchar phone "联系电话"
        varchar email "邮箱"
        varchar address "地址"
        varchar remark "备注"
        timestamp created_time "创建时间"
        timestamp updated_time "更新时间"
    }

    %% ========== 产品模块 ==========
    product {
        bigint id PK "商品ID"
        varchar name "商品名称"
        varchar code UK "商品编码"
        bigint category_id FK "所属分类ID"
        text description "商品描述"
        decimal price "商品价格"
        int stock_quantity "当前库存数量"
        int min_stock_level "最小安全库存阈值"
        varchar supplier "供应商名称（冗余）"
        int status "状态：0-下架，1-上架"
        timestamp in_time "上架时间"
        timestamp out_time "下架时间"
        timestamp created_time "创建时间"
        timestamp updated_time "更新时间"
    }

    %% 产品与分类的关系
    category ||--o{ product : "分类-商品"

    %% ========== 库存记录模块 ==========
    inventory_record {
        bigint id PK "库存记录ID"
        bigint product_id FK "关联商品ID"
        varchar type "操作类型：IN-入库，OUT-出库"
        int quantity "变动数量"
        varchar reason "变动原因"
        timestamp created_time "记录创建时间"
    }

    product ||--o{ inventory_record : "商品-库存记录"

    %% ========== 订单模块 ==========
    orders {
        bigint id PK "订单ID"
        varchar order_no UK "订单号"
        varchar customer_name "客户姓名"
        varchar customer_phone "客户联系电话"
        varchar status "订单状态"
        decimal total_amount "订单总金额"
        varchar remark "订单备注"
        timestamp created_time "订单创建时间"
        timestamp updated_time "订单更新时间"
    }

    %% ========== 订单项模块 ==========
    order_item {
        bigint id PK "订单项ID"
        bigint order_id FK "所属订单ID"
        bigint product_id FK "商品ID"
        varchar product_name "商品名称（冗余）"
        int quantity "购买数量"
        decimal price "下单时商品单价"
        decimal amount "项金额"
        varchar remark "项备注"
        timestamp created_time "创建时间"
    }

    orders ||--o{ order_item : "订单-订单项"
    product ||--o{ order_item : "商品-订单项"

    %% ========== 订单状态历史模块 ==========
    order_status_history {
        bigint id PK "状态历史ID"
        bigint order_id FK "对应订单ID"
        varchar from_status "变更前状态"
        varchar to_status "变更后状态"
        varchar reason "变更原因"
        timestamp changed_time "变更时间"
    }

    orders ||--o{ order_status_history : "订单-状态历史"

    %% ========== 供货记录模块 ==========
    supply_record {
        bigint id PK "供货记录ID"
        bigint supplier_id FK "供应商ID"
        bigint product_id FK "商品ID（可为空）"
        varchar product_name "商品名称（冗余）"
        int quantity "数量"
        decimal unit_price "单价"
        decimal total_amount "总金额"
        timestamp receive_time "收货时间"
        varchar remark "备注"
    }

    supplier ||--o{ supply_record : "供应商-供货记录"
    product ||--o{ supply_record : "商品-供货记录"

    %% ========== 图例说明 ==========
    %% PK: 主键 (Primary Key)
    %% FK: 外键 (Foreign Key)
    %% UK: 唯一索引 (Unique Key)
    %% 关系说明：
    %% ||--o{ : 一对多关系（一个父实体对应多个子实体）
    %% o{--|| : 多对一关系

