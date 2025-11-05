/*
 Navicat Premium Data Transfer

 Source Server         : root
 Source Server Type    : MySQL
 Source Server Version : 80027
 Source Host           : localhost:3306
 Source Schema         : ecommerce_warehouse

 Target Server Type    : MySQL
 Target Server Version : 80027
 File Encoding         : 65001

 Date: 05/11/2025 21:58:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyVhQy2S9S4Dq/9x5X8K8K8K8K8', '系统管理员', 'admin@ecommerce-warehouse.com', '13800138000', '2025-10-06 02:00:16', '2025-10-06 02:00:16');

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父级分类ID，NULL表示顶级分类',
  `level` int NOT NULL DEFAULT 1 COMMENT '分类层级：1-一级分类，2-二级分类，3-三级分类',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序字段',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` int NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `parent_id`(`parent_id` ASC) USING BTREE,
  CONSTRAINT `category_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `category` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '服装', NULL, 1, 1, '服装类商品', 1, '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (2, '男装', 1, 2, 1, '男士服装', 1, '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (3, '女装', 1, 2, 2, '女士服装', 1, '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (4, '短袖', 2, 3, 1, '男士短袖', 1, '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (5, '裤子', 2, 3, 2, '男士裤子', 1, '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (6, '鞋子', 2, 3, 3, '男士鞋子', 1, '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (7, '短袖', 3, 3, 1, '女士短袖', 1, '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (8, '长裙', 3, 3, 2, '女士长裙', 1, '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (9, '短裙', 3, 3, 3, '女士短裙', 1, '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (10, '连衣裙', 3, 3, 4, '女士连衣裙', 1, '2025-10-06 02:00:16', '2025-10-06 02:00:16');

-- ----------------------------
-- Table structure for inventory_record
-- ----------------------------
DROP TABLE IF EXISTS `inventory_record`;
CREATE TABLE `inventory_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'IN-入库，OUT-出库',
  `quantity` int NOT NULL COMMENT '数量',
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作原因',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作员ID',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  INDEX `operator_id`(`operator_id` ASC) USING BTREE,
  CONSTRAINT `inventory_record_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `inventory_record_ibfk_2` FOREIGN KEY (`operator_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory_record
-- ----------------------------
INSERT INTO `inventory_record` VALUES (1, 9, 'OUT', 20, '卖货1111', NULL, '2025-10-06 02:41:41');
INSERT INTO `inventory_record` VALUES (2, 1, 'OUT', 1, '1', NULL, '2025-10-06 02:55:38');
INSERT INTO `inventory_record` VALUES (3, 16, 'IN', 1, '11', NULL, '2025-10-06 03:16:41');

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `quantity` int NOT NULL COMMENT '数量',
  `price` decimal(10, 2) NOT NULL COMMENT '单价',
  `amount` decimal(10, 2) NOT NULL COMMENT '金额',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `order_id`(`order_id` ASC) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  CONSTRAINT `order_item_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `order_item_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item` VALUES (1, 1, 1, 'Nike运动短袖', 2, 199.00, 398.00, '蓝色L码', '2025-01-06 09:15:00');
INSERT INTO `order_item` VALUES (2, 2, 3, 'Levi\'s牛仔裤', 1, 399.00, 399.00, '黑色32码', '2025-01-06 10:30:00');
INSERT INTO `order_item` VALUES (3, 2, 5, 'Nike Air Max', 1, 899.00, 899.00, '白色42码', '2025-01-06 10:30:00');
INSERT INTO `order_item` VALUES (4, 3, 2, 'Adidas休闲短袖', 1, 179.00, 179.00, '白色M码', '2025-01-06 11:45:00');
INSERT INTO `order_item` VALUES (5, 4, 4, 'Uniqlo休闲裤', 2, 199.00, 398.00, '灰色L码', '2025-01-06 14:20:00');
INSERT INTO `order_item` VALUES (6, 4, 6, 'Adidas Stan Smith', 1, 599.00, 599.00, '白色41码', '2025-01-06 14:20:00');
INSERT INTO `order_item` VALUES (7, 5, 7, 'Zara基础短袖', 3, 99.00, 297.00, '黑色M码', '2025-01-06 15:10:00');
INSERT INTO `order_item` VALUES (8, 6, 8, 'H&M印花短袖', 1, 79.00, 79.00, '红色S码', '2025-01-06 16:30:00');
INSERT INTO `order_item` VALUES (9, 7, 9, '优衣库长裙', 2, 299.00, 598.00, '蓝色M码', '2025-01-06 17:45:00');
INSERT INTO `order_item` VALUES (10, 7, 10, 'Zara长裙', 1, 399.00, 399.00, '黑色L码', '2025-01-06 17:45:00');
INSERT INTO `order_item` VALUES (11, 8, 11, 'H&M短裙', 2, 149.00, 298.00, '粉色S码', '2025-01-06 18:20:00');
INSERT INTO `order_item` VALUES (12, 9, 12, 'Zara短裙', 1, 199.00, 199.00, '白色M码', '2025-01-06 19:15:00');
INSERT INTO `order_item` VALUES (13, 9, 13, '优衣库连衣裙', 1, 399.00, 399.00, '蓝色L码', '2025-01-06 19:15:00');
INSERT INTO `order_item` VALUES (14, 10, 14, 'Zara连衣裙', 1, 599.00, 599.00, '红色M码', '2025-01-06 20:00:00');
INSERT INTO `order_item` VALUES (15, 11, 2, 'Adidas休闲短袖', 1, 179.00, 179.00, NULL, '2025-10-06 02:32:24');
INSERT INTO `order_item` VALUES (16, 11, 3, 'Levi\'s牛仔裤', 1, 399.00, 399.00, NULL, '2025-10-06 02:32:24');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `customer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户姓名',
  `customer_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '联系电话',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单状态',
  `total_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作员ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `operator_id`(`operator_id` ASC) USING BTREE,
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`operator_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, 'ORD202501060001', '张三', '13800138001', 'COMPLETED', 398.00, 1, '客户要求加急配送', '2025-01-06 09:15:00', '2025-10-06 02:00:16');
INSERT INTO `orders` VALUES (2, 'ORD202501060002', '李四', '13800138002', 'PAID', 1298.00, 1, '客户指定颜色', '2025-01-06 10:30:00', '2025-10-06 03:14:27');
INSERT INTO `orders` VALUES (3, 'ORD202501060003', '王五', '13800138003', 'PAID', 179.00, 1, '首次购买', '2025-01-06 11:45:00', '2025-10-06 17:33:54');
INSERT INTO `orders` VALUES (4, 'ORD202501060004', '赵六', '13800138004', 'COMPLETED', 997.00, 1, 'VIP客户', '2025-01-06 14:20:00', '2025-10-06 02:00:16');
INSERT INTO `orders` VALUES (5, 'ORD202501060005', '钱七', '13800138005', 'PREPARING', 297.00, 1, '批量采购', '2025-01-06 15:10:00', '2025-10-06 17:55:47');
INSERT INTO `orders` VALUES (6, 'ORD202501060006', '孙八', '13800138006', 'CANCELLED', 79.00, 1, '客户取消', '2025-01-06 16:30:00', '2025-10-06 02:00:16');
INSERT INTO `orders` VALUES (7, 'ORD202501060007', '周九', '13800138007', 'COMPLETED', 997.00, 1, '企业采购', '2025-01-06 17:45:00', '2025-10-06 02:00:16');
INSERT INTO `orders` VALUES (8, 'ORD202501060008', '吴十', '13800138008', 'COMPLETED', 298.00, 1, '需要发票', '2025-01-06 18:20:00', '2025-10-06 02:23:23');
INSERT INTO `orders` VALUES (9, 'ORD202501060009', '郑十一', '13800138009', 'PREPARING', 598.00, 1, '海外客户', '2025-01-06 19:15:00', '2025-10-06 17:23:32');
INSERT INTO `orders` VALUES (10, 'ORD202501060010', '王十二', '13800138010', 'COMPLETED', 599.00, 1, '老客户推荐', '2025-01-06 20:00:00', '2025-10-06 02:00:16');
INSERT INTO `orders` VALUES (11, 'ORD202510060001', '老王', '15976775783', 'COMPLETED', 578.00, NULL, '', '2025-10-06 02:32:25', '2025-10-06 17:55:15');

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品编码',
  `category_id` bigint NOT NULL COMMENT '三级分类ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述',
  `price` decimal(10, 2) NOT NULL COMMENT '商品价格',
  `stock_quantity` int NOT NULL DEFAULT 0 COMMENT '库存数量',
  `min_stock_level` int NOT NULL DEFAULT 0 COMMENT '最低库存预警',
  `supplier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '供应商',
  `location` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存放位置',
  `status` int NOT NULL DEFAULT 1 COMMENT '商品状态：0-下架，1-上架',
  `stock_value` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '库存价值',
  `in_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
  `out_time` timestamp NULL DEFAULT NULL COMMENT '出库时间',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `category_id`(`category_id` ASC) USING BTREE,
  CONSTRAINT `product_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES (1, 'Nike运动短袖', 'P00001', 4, 'Nike经典运动短袖，100%纯棉', 199.00, 40, 10, 'Nike官方', 'A区-1层', 0, 9950.00, '2025-10-06 02:00:16', NULL, '2025-10-06 02:00:16', '2025-10-13 22:18:19');
INSERT INTO `product` VALUES (2, 'Adidas休闲短袖', 'P00002', 4, 'Adidas休闲短袖，透气舒适', 179.00, 28, 5, 'Adidas官方', 'A区-1层', 1, 5370.00, '2025-10-06 02:00:16', NULL, '2025-10-06 02:00:16', '2025-10-13 22:22:33');
INSERT INTO `product` VALUES (3, 'Levi\'s牛仔裤', 'P00003', 5, 'Levi\'s经典牛仔裤，修身版型', 399.00, 24, 5, 'Levi\'s官方', 'A区-2层', 1, 9975.00, '2025-10-06 02:00:16', NULL, '2025-10-06 02:00:16', '2025-10-13 22:22:33');
INSERT INTO `product` VALUES (4, 'Uniqlo休闲裤', 'P00004', 5, 'Uniqlo休闲裤，舒适百搭', 199.00, 40, 8, 'Uniqlo官方', 'A区-2层', 1, 7960.00, '2025-10-06 02:00:16', NULL, '2025-10-06 02:00:16', '2025-10-13 22:22:33');
INSERT INTO `product` VALUES (5, 'Nike Air Max', 'P00005', 6, 'Nike Air Max运动鞋，缓震舒适', 899.00, 20, 3, 'Nike官方', 'B区-1层', 1, 17980.00, '2025-10-06 02:00:16', NULL, '2025-10-06 02:00:16', '2025-10-13 22:22:33');
INSERT INTO `product` VALUES (6, 'Adidas Stan Smith', 'P00006', 6, 'Adidas Stan Smith经典小白鞋', 599.00, 15, 3, 'Adidas官方', 'B区-1层', 1, 8985.00, '2025-10-06 02:00:16', NULL, '2025-10-06 02:00:16', '2025-10-13 22:22:33');
INSERT INTO `product` VALUES (7, 'Zara基础短袖', 'P00007', 7, 'Zara基础短袖，简约百搭', 99.00, 63, 10, 'Zara官方', 'C区-1层', 1, 5940.00, '2025-10-06 02:00:16', NULL, '2025-10-06 02:00:16', '2025-10-13 22:22:33');
INSERT INTO `product` VALUES (8, 'H&M印花短袖', 'P00008', 7, 'H&M印花短袖，时尚潮流', 79.00, 45, 8, 'H&M官方', 'C区-1层', 1, 3555.00, '2025-10-06 02:00:16', NULL, '2025-10-06 02:00:16', '2025-10-13 22:22:33');
INSERT INTO `product` VALUES (9, '优衣库长裙', 'P00009', 8, '优衣库长裙，优雅大方', 299.00, 1, 5, '优衣库官方', 'C区-2层', 1, 5980.00, '2025-10-06 02:00:16', NULL, '2025-10-06 02:00:16', '2025-10-13 22:22:33');
INSERT INTO `product` VALUES (10, 'Zara长裙', 'P00010', 8, 'Zara长裙，时尚优雅', 399.00, 15, 3, 'Zara官方', 'C区-2层', 1, 5985.00, '2025-10-06 02:00:16', NULL, '2025-10-06 02:00:16', '2025-10-13 22:22:33');
INSERT INTO `product` VALUES (11, 'H&M短裙', 'P00011', 9, 'H&M短裙，青春活力', 149.00, 35, 8, 'H&M官方', 'C区-3层', 1, 5215.00, '2025-10-06 02:00:16', NULL, '2025-10-06 02:00:16', '2025-10-13 22:22:33');
INSERT INTO `product` VALUES (12, 'Zara短裙', 'P00012', 9, 'Zara短裙，时尚百搭', 199.00, 26, 5, 'Zara官方', 'C区-3层', 1, 4975.00, '2025-10-06 02:00:16', NULL, '2025-10-06 02:00:16', '2025-10-13 22:22:33');
INSERT INTO `product` VALUES (13, '优衣库连衣裙', 'P00013', 10, '优衣库连衣裙，简约优雅', 399.00, 19, 5, '优衣库官方', 'C区-4层', 1, 7182.00, '2025-10-06 02:00:16', NULL, '2025-10-06 02:00:16', '2025-10-13 22:22:33');
INSERT INTO `product` VALUES (14, 'Zara连衣裙', 'P00014', 10, 'Zara连衣裙，时尚设计', 599.00, 12, 3, 'Zara官方', 'C区-4层', 1, 7188.00, '2025-10-06 02:00:16', NULL, '2025-10-06 02:00:16', '2025-10-13 22:22:33');
INSERT INTO `product` VALUES (16, '360跑鞋', 'P00015', 6, '', 360.00, 1, 20, '360', '', 1, NULL, NULL, NULL, '2025-10-06 02:21:46', '2025-10-13 22:22:33');

-- ----------------------------
-- Triggers structure for table product
-- ----------------------------
DROP TRIGGER IF EXISTS `before_product_insert`;
delimiter ;;
CREATE TRIGGER `before_product_insert` BEFORE INSERT ON `product` FOR EACH ROW BEGIN
    SET NEW.code = CONCAT('P', LPAD(IFNULL((SELECT MAX(id) FROM product), 0) + 1, 5, '0'));
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
