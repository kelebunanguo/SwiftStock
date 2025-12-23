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

 Date: 23/12/2025 18:09:20
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员ID（主键）',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录用户名，唯一',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'bcrypt 加密后的密码哈希',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管理员显示姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管理员邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', '$2b$10$0zmHwE5t.L7JeSj70kcaKuW7AxdLFaTW8zUQX28gIW75NKY7XEneS', '系统管理员', 'admin@ecommerce-warehouse.com', '13800138000', '2025-10-06 02:00:16', '2025-12-21 10:03:58');

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID（主键）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父级分类ID，NULL表示顶级分类',
  `level` int NOT NULL DEFAULT 1 COMMENT '层级：1-一级，2-二级，3-三级',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '分类描述',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `parent_id`(`parent_id` ASC) USING BTREE,
  CONSTRAINT `category_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `category` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '服装', NULL, 1, 1, '服装类商品', '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (2, '男装', 1, 2, 1, '男士服装', '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (3, '女装', 1, 2, 2, '女士服装', '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (4, '短袖', 2, 3, 1, '男士短袖', '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (5, '裤子', 2, 3, 2, '男士裤子', '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (6, '鞋子', 2, 3, 3, '男士鞋子', '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (7, '短袖', 3, 3, 1, '女士短袖', '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (8, '长裙', 3, 3, 2, '女士长裙', '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (9, '短裙', 3, 3, 3, '女士短裙', '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (10, '连衣裙', 3, 3, 4, '女士连衣裙', '2025-10-06 02:00:16', '2025-10-06 02:00:16');
INSERT INTO `category` VALUES (11, '外套', 2, 3, 4, '男士外套', '2025-12-18 17:21:25', '2025-12-18 17:21:25');

-- ----------------------------
-- Table structure for inventory_record
-- ----------------------------
DROP TABLE IF EXISTS `inventory_record`;
CREATE TABLE `inventory_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存记录ID（主键）',
  `product_id` bigint NOT NULL COMMENT '关联商品ID（product.id）',
  `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型：IN-入库，OUT-出库',
  `quantity` int NOT NULL COMMENT '变动数量（正数）',
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变动原因/备注',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  CONSTRAINT `inventory_record_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory_record
-- ----------------------------
INSERT INTO `inventory_record` VALUES (1, 1, 'IN', 1, '采购\n', '2025-12-21 11:07:52');
INSERT INTO `inventory_record` VALUES (2, 3, 'IN', 30, '采购入库', '2025-12-23 18:06:53');
INSERT INTO `inventory_record` VALUES (3, 6, 'OUT', 10, '破损出库', '2025-12-23 18:09:05');

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单项ID（主键）',
  `order_id` bigint NOT NULL COMMENT '所属订单ID（orders.id）',
  `product_id` bigint NOT NULL COMMENT '商品ID（product.id）',
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称（冗余，便于历史回溯）',
  `quantity` int NOT NULL COMMENT '购买数量',
  `price` decimal(10, 2) NOT NULL COMMENT '下单时商品单价',
  `amount` decimal(10, 2) NOT NULL COMMENT '项金额（price * quantity）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '项备注（可选）',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `order_id`(`order_id` ASC) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  CONSTRAINT `order_item_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `order_item_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item` VALUES (1, 1, 1, 'Nike经典运动短袖', 2, 199.00, 398.00, '蓝色L码', '2025-12-15 09:30:00');
INSERT INTO `order_item` VALUES (2, 2, 18, 'Zara雪纺长裙', 1, 399.00, 399.00, '白色M码', '2025-12-15 10:15:00');
INSERT INTO `order_item` VALUES (3, 2, 17, 'H&M印花短袖', 2, 79.00, 158.00, '粉色S码', '2025-12-15 10:15:00');
INSERT INTO `order_item` VALUES (4, 3, 10, 'Adidas Ultra Boost', 1, 1299.00, 1299.00, '黑白配色 42码', '2025-12-15 11:00:00');
INSERT INTO `order_item` VALUES (5, 4, 9, 'Nike Air Max 270', 1, 899.00, 899.00, '灰色 43码', '2025-12-14 14:20:00');
INSERT INTO `order_item` VALUES (6, 5, 5, 'Levi\'s经典牛仔裤', 1, 399.00, 399.00, '深蓝色 32码', '2025-12-14 15:45:00');
INSERT INTO `order_item` VALUES (7, 5, 8, 'Nike运动长裤', 1, 279.00, 279.00, '黑色 L码', '2025-12-14 15:45:00');
INSERT INTO `order_item` VALUES (8, 6, 14, 'Adidas连帽卫衣', 2, 359.00, 718.00, '灰色 XL码', '2025-12-14 16:30:00');
INSERT INTO `order_item` VALUES (9, 6, 6, 'Uniqlo休闲裤', 2, 199.00, 398.00, '卡其色 L码', '2025-12-14 16:30:00');
INSERT INTO `order_item` VALUES (10, 7, 3, 'Puma彪马运动短袖', 3, 159.00, 477.00, '白色 M码', '2025-12-16 10:00:00');
INSERT INTO `order_item` VALUES (11, 8, 20, 'Zara复古连衣裙', 1, 599.00, 599.00, '碎花款 M码', '2025-12-16 11:30:00');
INSERT INTO `order_item` VALUES (12, 8, 17, 'H&M印花短袖', 2, 79.00, 158.00, '多色 S码', '2025-12-16 11:30:00');
INSERT INTO `order_item` VALUES (13, 9, 15, 'The North Face冲锋衣', 1, 1299.00, 1299.00, '红色 L码', '2025-12-13 13:45:00');
INSERT INTO `order_item` VALUES (14, 9, 5, 'Levi\'s经典牛仔裤', 1, 399.00, 399.00, '黑色 31码', '2025-12-13 13:45:00');
INSERT INTO `order_item` VALUES (15, 10, 11, 'Converse经典帆布鞋', 2, 299.00, 598.00, '黑色高帮 41码', '2025-12-13 15:20:00');
INSERT INTO `order_item` VALUES (16, 11, 19, '优衣库棉麻长裙', 2, 299.00, 598.00, '米色 M码', '2025-12-13 16:00:00');
INSERT INTO `order_item` VALUES (17, 12, 12, 'New Balance 574', 1, 599.00, 599.00, '灰色 42码', '2025-12-12 10:30:00');
INSERT INTO `order_item` VALUES (18, 12, 2, 'Adidas三叶草短袖', 1, 179.00, 179.00, '黑色 L码', '2025-12-12 10:30:00');
INSERT INTO `order_item` VALUES (19, 13, 13, 'Nike风衣外套', 2, 499.00, 998.00, '军绿色 XL码', '2025-12-12 14:00:00');
INSERT INTO `order_item` VALUES (20, 13, 6, 'Uniqlo休闲裤', 2, 199.00, 398.00, '黑色 L码', '2025-12-12 14:00:00');
INSERT INTO `order_item` VALUES (21, 14, 7, 'Jack&Jones修身西裤', 2, 349.00, 698.00, '藏青色 33码', '2025-12-13 09:30:00');
INSERT INTO `order_item` VALUES (22, 15, 14, 'Adidas连帽卫衣', 2, 359.00, 718.00, '黑色 XXL码', '2025-12-13 11:00:00');
INSERT INTO `order_item` VALUES (23, 15, 4, 'Under Armour训练短袖', 2, 229.00, 458.00, '蓝色 L码', '2025-12-13 11:00:00');
INSERT INTO `order_item` VALUES (24, 16, 13, 'Nike风衣外套', 2, 499.00, 998.00, '黑色 L码', '2025-12-14 10:00:00');
INSERT INTO `order_item` VALUES (25, 17, 10, 'Adidas Ultra Boost', 1, 1299.00, 1299.00, '全黑 43码', '2025-12-15 11:30:00');
INSERT INTO `order_item` VALUES (26, 17, 16, 'Zara基础短袖', 3, 99.00, 297.00, '白色 M码', '2025-12-15 11:30:00');
INSERT INTO `order_item` VALUES (27, 18, 16, 'Zara基础短袖', 3, 99.00, 297.00, '黑色 M码', '2025-12-17 13:00:00');
INSERT INTO `order_item` VALUES (28, 19, 5, 'Levi\'s经典牛仔裤', 1, 399.00, 399.00, '浅蓝色 30码', '2025-12-17 14:00:00');
INSERT INTO `order_item` VALUES (29, 20, 9, 'Nike Air Max 270', 1, 899.00, 899.00, '白色 44码', '2025-12-16 14:00:00');
INSERT INTO `order_item` VALUES (30, 21, 1, 'Nike经典运动短袖', 60, 199.00, 11940.00, NULL, '2025-12-18 19:01:17');
INSERT INTO `order_item` VALUES (31, 22, 21, 'DSP薛之谦同款外套', 4, 299.00, 1196.00, NULL, '2025-12-18 19:03:45');
INSERT INTO `order_item` VALUES (32, 23, 22, 'DSP薛之谦同款马丁靴', 20, 299.00, 5980.00, NULL, '2025-12-22 18:47:12');
INSERT INTO `order_item` VALUES (33, 24, 22, 'DSP薛之谦同款马丁靴', 20, 299.00, 5980.00, NULL, '2025-12-22 18:54:58');
INSERT INTO `order_item` VALUES (34, 25, 22, 'DSP薛之谦同款马丁靴', 20, 299.00, 5980.00, NULL, '2025-12-22 19:30:01');
INSERT INTO `order_item` VALUES (35, 26, 2, 'Adidas三叶草短袖', 65, 179.00, 11635.00, NULL, '2025-12-22 19:35:01');

-- ----------------------------
-- Table structure for order_status_history
-- ----------------------------
DROP TABLE IF EXISTS `order_status_history`;
CREATE TABLE `order_status_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '状态历史ID（主键）',
  `order_id` bigint NOT NULL COMMENT '对应订单ID（orders.id）',
  `from_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更前状态（可能为空）',
  `to_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '变更后状态',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '变更原因/备注',
  `changed_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  CONSTRAINT `fk_order_status_history_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 81 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_status_history
-- ----------------------------
INSERT INTO `order_status_history` VALUES (1, 1, NULL, 'UNPAID', '订单创建', '2025-12-15 09:30:00');
INSERT INTO `order_status_history` VALUES (2, 2, NULL, 'UNPAID', '订单创建', '2025-12-15 10:15:00');
INSERT INTO `order_status_history` VALUES (3, 3, NULL, 'UNPAID', '订单创建', '2025-12-15 11:00:00');
INSERT INTO `order_status_history` VALUES (4, 4, NULL, 'UNPAID', '订单创建', '2025-12-14 14:20:00');
INSERT INTO `order_status_history` VALUES (5, 4, 'UNPAID', 'PAID', '客户已付款', '2025-12-15 08:30:00');
INSERT INTO `order_status_history` VALUES (6, 5, NULL, 'UNPAID', '订单创建', '2025-12-14 15:45:00');
INSERT INTO `order_status_history` VALUES (7, 5, 'UNPAID', 'PAID', '客户已付款', '2025-12-15 09:00:00');
INSERT INTO `order_status_history` VALUES (8, 6, NULL, 'UNPAID', '订单创建', '2025-12-14 16:30:00');
INSERT INTO `order_status_history` VALUES (9, 6, 'UNPAID', 'PAID', '客户已付款', '2025-12-15 09:15:00');
INSERT INTO `order_status_history` VALUES (10, 7, NULL, 'UNPAID', '订单创建', '2025-12-14 10:00:00');
INSERT INTO `order_status_history` VALUES (11, 7, 'UNPAID', 'PAID', '客户已付款', '2025-12-14 18:00:00');
INSERT INTO `order_status_history` VALUES (12, 7, 'PAID', 'PREPARING', '开始配货', '2025-12-15 08:00:00');
INSERT INTO `order_status_history` VALUES (13, 8, NULL, 'UNPAID', '订单创建', '2025-12-14 11:30:00');
INSERT INTO `order_status_history` VALUES (14, 8, 'UNPAID', 'PAID', '客户已付款', '2025-12-14 19:00:00');
INSERT INTO `order_status_history` VALUES (15, 8, 'PAID', 'PREPARING', '开始配货', '2025-12-15 09:30:00');
INSERT INTO `order_status_history` VALUES (16, 9, NULL, 'UNPAID', '订单创建', '2025-12-13 13:45:00');
INSERT INTO `order_status_history` VALUES (17, 9, 'UNPAID', 'PAID', '客户已付款', '2025-12-13 20:00:00');
INSERT INTO `order_status_history` VALUES (18, 9, 'PAID', 'PREPARING', '开始配货', '2025-12-14 09:00:00');
INSERT INTO `order_status_history` VALUES (19, 9, 'PREPARING', 'SHIPPED', '顺丰快递已发货', '2025-12-14 14:00:00');
INSERT INTO `order_status_history` VALUES (20, 10, NULL, 'UNPAID', '订单创建', '2025-12-13 15:20:00');
INSERT INTO `order_status_history` VALUES (21, 10, 'UNPAID', 'PAID', '客户已付款', '2025-12-13 21:00:00');
INSERT INTO `order_status_history` VALUES (22, 10, 'PAID', 'PREPARING', '开始配货', '2025-12-14 10:00:00');
INSERT INTO `order_status_history` VALUES (23, 10, 'PREPARING', 'SHIPPED', '圆通快递已发货', '2025-12-14 15:30:00');
INSERT INTO `order_status_history` VALUES (24, 11, NULL, 'UNPAID', '订单创建', '2025-12-13 16:00:00');
INSERT INTO `order_status_history` VALUES (25, 11, 'UNPAID', 'PAID', '客户已付款', '2025-12-13 22:00:00');
INSERT INTO `order_status_history` VALUES (26, 11, 'PAID', 'PREPARING', '开始配货', '2025-12-14 11:00:00');
INSERT INTO `order_status_history` VALUES (27, 11, 'PREPARING', 'SHIPPED', '京东快递已发货', '2025-12-14 16:00:00');
INSERT INTO `order_status_history` VALUES (28, 12, NULL, 'UNPAID', '订单创建', '2025-12-10 10:30:00');
INSERT INTO `order_status_history` VALUES (29, 12, 'UNPAID', 'PAID', '客户已付款', '2025-12-10 16:00:00');
INSERT INTO `order_status_history` VALUES (30, 12, 'PAID', 'PREPARING', '开始配货', '2025-12-11 09:00:00');
INSERT INTO `order_status_history` VALUES (31, 12, 'PREPARING', 'SHIPPED', '已发货', '2025-12-11 14:00:00');
INSERT INTO `order_status_history` VALUES (32, 12, 'SHIPPED', 'DELIVERED', '客户已签收', '2025-12-12 17:00:00');
INSERT INTO `order_status_history` VALUES (33, 13, NULL, 'UNPAID', '订单创建', '2025-12-10 14:00:00');
INSERT INTO `order_status_history` VALUES (34, 13, 'UNPAID', 'PAID', '客户已付款', '2025-12-10 18:00:00');
INSERT INTO `order_status_history` VALUES (35, 13, 'PAID', 'PREPARING', '开始配货', '2025-12-11 10:00:00');
INSERT INTO `order_status_history` VALUES (36, 13, 'PREPARING', 'SHIPPED', '已发货', '2025-12-11 15:00:00');
INSERT INTO `order_status_history` VALUES (37, 13, 'SHIPPED', 'DELIVERED', '客户已签收', '2025-12-12 18:20:00');
INSERT INTO `order_status_history` VALUES (38, 14, NULL, 'UNPAID', '订单创建', '2025-12-11 09:30:00');
INSERT INTO `order_status_history` VALUES (39, 14, 'UNPAID', 'PAID', '客户已付款', '2025-12-11 15:00:00');
INSERT INTO `order_status_history` VALUES (40, 14, 'PAID', 'PREPARING', '开始配货', '2025-12-12 08:00:00');
INSERT INTO `order_status_history` VALUES (41, 14, 'PREPARING', 'SHIPPED', '已发货', '2025-12-12 13:00:00');
INSERT INTO `order_status_history` VALUES (42, 14, 'SHIPPED', 'DELIVERED', '客户已签收', '2025-12-13 10:00:00');
INSERT INTO `order_status_history` VALUES (43, 15, NULL, 'UNPAID', '订单创建', '2025-12-11 11:00:00');
INSERT INTO `order_status_history` VALUES (44, 15, 'UNPAID', 'PAID', '客户已付款', '2025-12-11 16:00:00');
INSERT INTO `order_status_history` VALUES (45, 15, 'PAID', 'PREPARING', '开始配货', '2025-12-12 09:00:00');
INSERT INTO `order_status_history` VALUES (46, 15, 'PREPARING', 'SHIPPED', '已发货', '2025-12-12 14:00:00');
INSERT INTO `order_status_history` VALUES (47, 15, 'SHIPPED', 'DELIVERED', '客户已签收', '2025-12-13 14:30:00');
INSERT INTO `order_status_history` VALUES (48, 16, NULL, 'UNPAID', '订单创建', '2025-12-08 10:00:00');
INSERT INTO `order_status_history` VALUES (49, 16, 'UNPAID', 'PAID', '客户已付款', '2025-12-08 15:00:00');
INSERT INTO `order_status_history` VALUES (50, 16, 'PAID', 'PREPARING', '开始配货', '2025-12-09 09:00:00');
INSERT INTO `order_status_history` VALUES (51, 16, 'PREPARING', 'SHIPPED', '已发货', '2025-12-09 14:00:00');
INSERT INTO `order_status_history` VALUES (52, 16, 'SHIPPED', 'DELIVERED', '客户已签收', '2025-12-11 10:00:00');
INSERT INTO `order_status_history` VALUES (53, 16, 'DELIVERED', 'COMPLETED', '订单已完成', '2025-12-15 10:00:00');
INSERT INTO `order_status_history` VALUES (54, 17, NULL, 'UNPAID', '订单创建', '2025-12-09 11:30:00');
INSERT INTO `order_status_history` VALUES (55, 17, 'UNPAID', 'PAID', '客户已付款', '2025-12-09 16:00:00');
INSERT INTO `order_status_history` VALUES (56, 17, 'PAID', 'PREPARING', '开始配货', '2025-12-10 09:00:00');
INSERT INTO `order_status_history` VALUES (57, 17, 'PREPARING', 'SHIPPED', '已发货', '2025-12-10 14:00:00');
INSERT INTO `order_status_history` VALUES (58, 17, 'SHIPPED', 'DELIVERED', '客户已签收', '2025-12-12 11:00:00');
INSERT INTO `order_status_history` VALUES (59, 17, 'DELIVERED', 'COMPLETED', '订单已完成', '2025-12-15 11:30:00');
INSERT INTO `order_status_history` VALUES (60, 18, NULL, 'UNPAID', '订单创建', '2025-12-14 13:00:00');
INSERT INTO `order_status_history` VALUES (61, 18, 'UNPAID', 'CANCELLED', '客户申请取消', '2025-12-14 18:00:00');
INSERT INTO `order_status_history` VALUES (62, 19, NULL, 'UNPAID', '订单创建', '2025-12-14 14:00:00');
INSERT INTO `order_status_history` VALUES (63, 19, 'UNPAID', 'CANCELLED', '库存不足，无法发货', '2025-12-14 19:00:00');
INSERT INTO `order_status_history` VALUES (64, 20, NULL, 'UNPAID', '订单创建', '2025-12-08 14:00:00');
INSERT INTO `order_status_history` VALUES (65, 20, 'UNPAID', 'PAID', '客户已付款', '2025-12-08 18:00:00');
INSERT INTO `order_status_history` VALUES (66, 20, 'PAID', 'PREPARING', '开始配货', '2025-12-09 09:00:00');
INSERT INTO `order_status_history` VALUES (67, 20, 'PREPARING', 'SHIPPED', '已发货', '2025-12-09 14:00:00');
INSERT INTO `order_status_history` VALUES (68, 20, 'SHIPPED', 'DELIVERED', '客户已签收', '2025-12-11 15:00:00');
INSERT INTO `order_status_history` VALUES (69, 20, 'DELIVERED', 'REFUNDED', '商品有质量问题，已退货退款', '2025-12-16 10:00:00');
INSERT INTO `order_status_history` VALUES (70, 21, NULL, 'PAID', '订单创建', '2025-12-18 19:01:18');
INSERT INTO `order_status_history` VALUES (71, 22, NULL, 'UNPAID', '订单创建', '2025-12-18 19:03:45');
INSERT INTO `order_status_history` VALUES (72, 22, 'UNPAID', 'PAID', '客户付款', '2025-12-18 19:03:56');
INSERT INTO `order_status_history` VALUES (73, 23, NULL, 'UNPAID', '订单创建', '2025-12-22 18:47:12');
INSERT INTO `order_status_history` VALUES (74, 23, 'UNPAID', 'CANCELLED', '客户不要了', '2025-12-22 18:54:25');
INSERT INTO `order_status_history` VALUES (75, 24, NULL, 'UNPAID', '订单创建', '2025-12-22 18:54:59');
INSERT INTO `order_status_history` VALUES (76, 24, 'UNPAID', 'CANCELLED', '又不要了', '2025-12-22 19:13:36');
INSERT INTO `order_status_history` VALUES (77, 25, NULL, 'UNPAID', '订单创建', '2025-12-22 19:30:02');
INSERT INTO `order_status_history` VALUES (78, 25, 'UNPAID', 'PAID', '客户付款', '2025-12-22 19:30:06');
INSERT INTO `order_status_history` VALUES (79, 26, NULL, 'PAID', '订单创建', '2025-12-22 19:35:01');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID（主键）',
  `order_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号，系统生成或前端传入，唯一',
  `customer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户姓名/收货人',
  `customer_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户联系电话',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单状态（如 UNPAID/PAID/SHIPPED 等）',
  `total_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单备注/说明',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 28 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, 'ORD20251218000101', '张伟', '13800001001', 'UNPAID', 398.00, '客户要求确认后立即发货', '2025-12-15 09:30:00', '2025-12-22 19:27:02');
INSERT INTO `orders` VALUES (2, 'ORD20251218000202', '李娜娜', '13800001002', 'UNPAID', 598.00, '新客户首单', '2025-12-15 10:15:00', '2025-12-22 19:27:06');
INSERT INTO `orders` VALUES (3, 'ORD20251218000303', '王小强', '13800001003', 'UNPAID', 1299.00, '', '2025-12-15 11:00:00', '2025-12-22 19:27:09');
INSERT INTO `orders` VALUES (4, 'ORD20251218000404', '刘贵芳', '13800001004', 'PAID', 899.00, '客户要求加急处理', '2025-12-14 14:20:00', '2025-12-22 19:27:14');
INSERT INTO `orders` VALUES (5, 'ORD20251218000505', '陈嘉明', '13800001005', 'PAID', 678.00, 'VIP客户', '2025-12-14 15:45:00', '2025-12-22 19:27:17');
INSERT INTO `orders` VALUES (6, 'ORD20251218000606', '赵美丽', '13800001006', 'PAID', 1098.00, '', '2025-12-14 16:30:00', '2025-12-22 19:27:20');
INSERT INTO `orders` VALUES (7, 'ORD20251218000707', '黄安磊', '13800001007', 'PREPARING', 478.00, '正在配货，预计今日发货', '2025-12-16 10:00:00', '2025-12-22 19:27:23');
INSERT INTO `orders` VALUES (8, 'ORD20251218000808', '周杰', '13800001008', 'PREPARING', 798.00, '正在拣货中', '2025-12-16 11:30:00', '2025-12-22 19:27:26');
INSERT INTO `orders` VALUES (9, 'ORD20251218000909', '吴言祖', '13800001009', 'SHIPPED', 1697.00, '快递单号：SF1234567890', '2025-12-13 13:45:00', '2025-12-22 19:27:28');
INSERT INTO `orders` VALUES (10, 'ORD20251218001010', '郑秀文', '13800001010', 'SHIPPED', 598.00, '快递单号：YTO9876543210', '2025-12-13 15:20:00', '2025-12-22 19:27:30');
INSERT INTO `orders` VALUES (11, 'ORD20251218001111', '孙晓梅', '13800001011', 'SHIPPED', 598.00, '快递单号：JD2345678901', '2025-12-13 16:00:00', '2025-12-22 19:27:31');
INSERT INTO `orders` VALUES (12, 'ORD20251218001212', '马六六', '13800001012', 'DELIVERED', 898.00, '客户已签收，五星好评', '2025-12-12 10:30:00', '2025-12-22 19:27:32');
INSERT INTO `orders` VALUES (13, 'ORD20251218001313', '林青霞', '13800001013', 'DELIVERED', 1498.00, '客户确认收货', '2025-12-12 14:00:00', '2025-12-22 19:27:33');
INSERT INTO `orders` VALUES (14, 'ORD20251218001414', '胡嘉清', '13800001014', 'DELIVERED', 698.00, '老客户回购', '2025-12-13 09:30:00', '2025-12-22 19:27:34');
INSERT INTO `orders` VALUES (15, 'ORD20251218001515', '徐嘉铭', '13800001015', 'DELIVERED', 1198.00, '企业采购', '2025-12-13 11:00:00', '2025-12-22 19:27:36');
INSERT INTO `orders` VALUES (16, 'ORD20251218001616', '高圆园', '13800001016', 'COMPLETED', 999.00, '订单已完成，客户好评', '2025-12-14 10:00:00', '2025-12-22 19:27:39');
INSERT INTO `orders` VALUES (17, 'ORD20251218001717', '邓爽', '13800001017', 'COMPLETED', 1599.00, '订单已完成', '2025-12-15 11:30:00', '2025-12-22 19:27:41');
INSERT INTO `orders` VALUES (18, 'ORD20251218001818', '孙百川', '13800001018', 'CANCELLED', 299.00, '客户申请取消', '2025-12-17 13:00:00', '2025-12-22 19:27:42');
INSERT INTO `orders` VALUES (19, 'ORD20251218001919', '范冰', '13800001019', 'CANCELLED', 399.00, '库存不足，无法发货', '2025-12-17 14:00:00', '2025-12-22 19:27:45');
INSERT INTO `orders` VALUES (20, 'ORD20251218002020', '李冰', '13800001020', 'REFUNDED', 899.00, '商品有质量问题，已退货退款', '2025-12-16 14:00:00', '2025-12-22 19:27:47');
INSERT INTO `orders` VALUES (21, 'ORD20251218002121', '测试大单客户', '13800138001', 'PAID', 11940.00, NULL, '2025-12-18 19:01:17', '2025-12-22 19:27:48');
INSERT INTO `orders` VALUES (22, 'ORD20251218002222', '薛之谦粉丝', '13800138002', 'PAID', 1196.00, NULL, '2025-12-18 19:03:45', '2025-12-22 19:27:51');
INSERT INTO `orders` VALUES (23, 'ORD20251222383305', '小薛', '17777770717', 'CANCELLED', 5980.00, '小薛老板', '2025-12-22 18:47:12', '2025-12-22 18:54:25');
INSERT INTO `orders` VALUES (24, 'ORD20251222873735', '小薛老板', '17777770717', 'CANCELLED', 5980.00, '小薛老板', '2025-12-22 18:54:59', '2025-12-22 19:13:36');
INSERT INTO `orders` VALUES (25, 'ORD20251222976219', '小徐', '15219120717', 'PAID', 5980.00, '大客户', '2025-12-22 19:30:02', '2025-12-22 19:30:06');
INSERT INTO `orders` VALUES (26, 'ORD20251222104502', '金元宝', '13422223333', 'PAID', 11635.00, '', '2025-12-22 19:35:01', NULL);

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID（主键）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品编码（唯一）',
  `category_id` bigint NOT NULL COMMENT '所属分类ID（category.id）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述/详情',
  `price` decimal(10, 2) NOT NULL COMMENT '商品价格',
  `stock_quantity` int NOT NULL DEFAULT 0 COMMENT '当前库存数量',
  `min_stock_level` int NOT NULL DEFAULT 0 COMMENT '最小安全库存阈值',
  `supplier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '供应商名称/标识（冗余）',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
  `in_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上架/入库时间（参考）',
  `out_time` timestamp NULL DEFAULT NULL COMMENT '下架/出库时间（参考）',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `category_id`(`category_id` ASC) USING BTREE,
  CONSTRAINT `product_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES (1, 'Nike经典运动短袖', 'P00001', 4, '源自Nike官方旗舰店，这款经典运动短袖将专业运动基因融入日常穿搭。采用品牌专属的Dri-FIT速干科技面料，轻薄透气且排汗迅速，时刻保持肌肤干爽舒适。经典圆领与微弹剪裁设计，兼顾运动灵活性与日常修身感，肩部走线采用高端工艺加固，耐穿不变形。\n无论是健身房高强度训练、户外跑步，还是休闲出行，它都能轻松应对。面料触感柔软亲肤，配合立体版型带来无束缚的穿着体验。建议冷水机洗，避免长时间浸泡，自然晾干可更好地保持面料性能与色泽。一件兼具功能与风格的百搭经典，助你自信驾驭多种场合。', 199.00, 1, 10, 'Nike官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-22 15:14:43');
INSERT INTO `product` VALUES (2, 'Adidas三叶草短袖', 'P00002', 4, 'Adidas经典三叶草logo短袖，时尚百搭', 179.00, 0, 10, 'Adidas官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-22 19:35:01');
INSERT INTO `product` VALUES (3, 'Puma彪马运动短袖', 'P00003', 4, 'Puma运动短袖，舒适透气，运动首选', 159.00, 80, 8, 'Puma官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-23 18:06:53');
INSERT INTO `product` VALUES (4, 'Under Armour训练短袖', 'P00004', 4, 'UA专业训练短袖，速干面料', 229.00, 45, 8, 'UA官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:18');
INSERT INTO `product` VALUES (5, 'Levi\'s经典牛仔裤', 'P00005', 5, 'Levi\'s 501经典直筒牛仔裤，永不过时', 399.00, 55, 8, 'Levi\'s官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:21');
INSERT INTO `product` VALUES (6, 'Uniqlo休闲裤', 'P00006', 5, '优衣库舒适休闲裤，四季可穿', 199.00, 65, 10, 'Uniqlo官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-23 18:09:05');
INSERT INTO `product` VALUES (7, 'Jack&Jones修身西裤', 'P00007', 5, 'JJ商务修身西裤，职场首选', 349.00, 40, 5, 'JJ官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:28');
INSERT INTO `product` VALUES (8, 'Nike运动长裤', 'P00008', 5, 'Nike专业运动长裤，弹力面料', 279.00, 60, 8, 'Nike官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:30');
INSERT INTO `product` VALUES (9, 'Nike Air Max 270', 'P00009', 6, 'Nike Air Max 270气垫运动鞋，时尚舒适', 899.00, 35, 5, 'Nike官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:33');
INSERT INTO `product` VALUES (10, 'Adidas Ultra Boost', 'P00010', 6, 'Adidas Ultra Boost爆米花跑鞋，缓震极佳', 1299.00, 25, 5, 'Adidas官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:36');
INSERT INTO `product` VALUES (11, 'Converse经典帆布鞋', 'P00011', 6, 'Converse All Star经典帆布鞋，百搭神器', 299.00, 90, 15, 'Converse官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:38');
INSERT INTO `product` VALUES (12, 'New Balance 574', 'P00012', 6, 'New Balance 574复古跑鞋，经典配色', 599.00, 45, 8, 'NB官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:39');
INSERT INTO `product` VALUES (13, 'Nike风衣外套', 'P00013', 11, 'Nike防风运动外套，轻薄便携', 499.00, 50, 8, 'Nike官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:42');
INSERT INTO `product` VALUES (14, 'Adidas连帽卫衣', 'P00014', 11, 'Adidas经典连帽卫衣，保暖舒适', 359.00, 65, 10, 'Adidas官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:44');
INSERT INTO `product` VALUES (15, 'The North Face冲锋衣', 'P00015', 11, 'TNF专业户外冲锋衣，防水透气', 1299.00, 30, 5, 'TNF官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:47');
INSERT INTO `product` VALUES (16, 'Zara基础短袖', 'P00016', 7, 'Zara基础款短袖，简约百搭', 99.00, 100, 15, 'Zara官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:50');
INSERT INTO `product` VALUES (17, 'H&M印花短袖', 'P00017', 7, 'H&M时尚印花短袖，青春活力', 79.00, 85, 12, 'H&M官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:53');
INSERT INTO `product` VALUES (18, 'Zara雪纺长裙', 'P00018', 8, 'Zara优雅雪纺长裙，仙气飘飘', 399.00, 40, 8, 'Zara官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:55');
INSERT INTO `product` VALUES (19, '优衣库棉麻长裙', 'P00019', 8, '优衣库舒适棉麻长裙，夏季必备', 299.00, 55, 10, 'Uniqlo官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:57');
INSERT INTO `product` VALUES (20, 'Zara复古连衣裙', 'P00020', 10, 'Zara复古风连衣裙，气质优雅', 599.00, 35, 5, 'Zara官方旗舰店', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:32:04');
INSERT INTO `product` VALUES (21, 'DSP薛之谦同款外套', 'P00021', 11, 'DSP薛之谦同款潮流外套', 299.00, 96, 10, 'DSP官方', 1, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:32:08');
INSERT INTO `product` VALUES (22, 'DSP薛之谦同款马丁靴', 'P00022', 6, '【DSP官方出品】明星同款马丁靴，演绎街头潮流新风尚。采用高端工艺打造，鞋面精选优质耐磨皮革，内里搭载轻薄透气网眼材质，结合独家速干科技，时刻保持双脚干爽舒适。经典系带设计搭配金属孔眼，细节彰显品质；加固鞋头与防滑橡胶大底，提供出色支撑与抓地力，无惧多种地形。  \n无论是音乐节狂欢、城市漫游还是日常穿搭，这双靴子都能轻松驾驭，百搭经典造型提升整体格调。上脚体验贴合不磨脚，行走稳健轻盈。建议使用软布擦拭清洁，自然阴干，避免暴晒以延长靴子寿命。穿上DSP马丁靴，步履间尽显个性态度！', 299.00, 0, 10, 'DSP官方', 1, NULL, NULL, '2025-12-22 15:19:11', '2025-12-22 19:30:06');

-- ----------------------------
-- Table structure for supplier
-- ----------------------------
DROP TABLE IF EXISTS `supplier`;
CREATE TABLE `supplier`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应商ID（主键）',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商名称（唯一索引）',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uniq_supplier_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of supplier
-- ----------------------------
INSERT INTO `supplier` VALUES (1, 'Nike官方旗舰店', '李经理', '13600293696', 'nike@contact.com', '北京市朝阳区望京街道1号', NULL, '2025-12-21 11:53:48', '2025-12-21 12:10:29');
INSERT INTO `supplier` VALUES (2, 'Adidas官方旗舰店', '王经理', '13825555730', 'adidas@contact.com', '上海市浦东新区世纪大道100号', NULL, '2025-12-21 11:53:48', '2025-12-21 12:10:34');
INSERT INTO `supplier` VALUES (3, 'Puma官方旗舰店', '赵经理', '15240242232', 'puma@contact.com', '深圳市南山区科技园路8号', NULL, '2025-12-21 11:53:48', '2025-12-21 12:10:38');
INSERT INTO `supplier` VALUES (4, 'UA官方旗舰店', '陈经理', '13999514275', 'ua@contact.com', '广州市天河区体育西路20号', NULL, '2025-12-21 11:53:48', '2025-12-21 12:10:42');
INSERT INTO `supplier` VALUES (5, 'Levi\'s官方旗舰店', '周经理', '15794571460', 'levis@contact.com', '杭州市西湖区文一路50号', NULL, '2025-12-21 11:53:48', '2025-12-21 12:10:46');
INSERT INTO `supplier` VALUES (6, 'Uniqlo官方旗舰店', '孙经理', '13682688580', 'uniqo@contact.com', '南京市鼓楼区中山路10号', NULL, '2025-12-21 11:53:48', '2025-12-21 12:10:50');
INSERT INTO `supplier` VALUES (7, 'JJ官方旗舰店', '高经理', '13844917597', 'jj@contact.com', '苏州市工业园区创业路3号', NULL, '2025-12-21 11:53:48', '2025-12-21 12:10:53');
INSERT INTO `supplier` VALUES (8, 'Converse官方旗舰店', '刘经理', '13538725360', 'converse@contact.com', '成都市锦江区春熙路1号', NULL, '2025-12-21 11:53:48', '2025-12-21 12:10:57');
INSERT INTO `supplier` VALUES (9, 'NB官方旗舰店', '何经理', '15112850235', 'nb@contact.com', '重庆市渝中区解放碑10号', NULL, '2025-12-21 11:53:48', '2025-12-21 12:11:02');
INSERT INTO `supplier` VALUES (10, 'TNF官方旗舰店', '徐经理', '13655162950', 'tnf@contact.com', '西安市碑林区长安北路5号', NULL, '2025-12-21 11:53:48', '2025-12-21 12:11:08');
INSERT INTO `supplier` VALUES (11, 'Zara官方旗舰店', '张经理', '13711503883', 'zara@contact.com', '杭州市西湖区湖滨路2号', NULL, '2025-12-21 11:53:48', '2025-12-21 12:11:12');
INSERT INTO `supplier` VALUES (12, 'H&M官方旗舰店', '邹经理', '15257103857', 'hm@contact.com', '北京市朝阳区三里屯路8号', NULL, '2025-12-21 11:53:48', '2025-12-21 12:11:14');
INSERT INTO `supplier` VALUES (13, 'DSP官方', '薛经理', '17707170717', 'dsp@contact.com', '上海市静安区南京西路1618号久光百货四楼D426-3', NULL, '2025-12-21 11:53:48', '2025-12-21 12:11:43');

-- ----------------------------
-- Table structure for supply_record
-- ----------------------------
DROP TABLE IF EXISTS `supply_record`;
CREATE TABLE `supply_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '供货记录ID（主键）',
  `supplier_id` bigint NOT NULL COMMENT '供应商ID（supplier.id）',
  `product_id` bigint NULL DEFAULT NULL COMMENT '商品ID（product.id，可为空）',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品名称冗余',
  `quantity` int NOT NULL COMMENT '数量',
  `unit_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '单价',
  `total_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '总金额（unit_price * quantity）',
  `receive_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收货时间/记录时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注/说明',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_supplier_id`(`supplier_id` ASC) USING BTREE,
  CONSTRAINT `fk_supply_record_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of supply_record
-- ----------------------------
INSERT INTO `supply_record` VALUES (1, 1, 1, 'Nike经典运动短袖', 1, 199.00, 199.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (2, 2, 2, 'Adidas三叶草短袖', 65, 179.00, 11635.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (3, 3, 3, 'Puma彪马运动短袖', 50, 159.00, 7950.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (4, 4, 4, 'Under Armour训练短袖', 45, 229.00, 10305.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (5, 5, 5, 'Levi\'s经典牛仔裤', 55, 399.00, 21945.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (6, 6, 6, 'Uniqlo休闲裤', 75, 199.00, 14925.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (7, 7, 7, 'Jack&Jones修身西裤', 40, 349.00, 13960.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (8, 1, 8, 'Nike运动长裤', 60, 279.00, 16740.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (9, 1, 9, 'Nike Air Max 270', 35, 899.00, 31465.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (10, 2, 10, 'Adidas Ultra Boost', 25, 1299.00, 32475.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (11, 8, 11, 'Converse经典帆布鞋', 90, 299.00, 26910.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (12, 9, 12, 'New Balance 574', 45, 599.00, 26955.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (13, 1, 13, 'Nike风衣外套', 50, 499.00, 24950.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (14, 2, 14, 'Adidas连帽卫衣', 65, 359.00, 23335.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (15, 10, 15, 'The North Face冲锋衣', 30, 1299.00, 38970.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (16, 11, 16, 'Zara基础短袖', 100, 99.00, 9900.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (17, 12, 17, 'H&M印花短袖', 85, 79.00, 6715.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (18, 11, 18, 'Zara雪纺长裙', 40, 399.00, 15960.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (19, 6, 19, '优衣库棉麻长裙', 55, 299.00, 16445.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (20, 11, 20, 'Zara复古连衣裙', 35, 599.00, 20965.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (21, 13, 21, 'DSP薛之谦同款外套', 96, 299.00, 28704.00, '2025-12-18 18:33:39', '初始化入库（来自 product.stock_quantity）');
INSERT INTO `supply_record` VALUES (32, 13, 22, 'DSP薛之谦同款马丁靴', 20, 299.00, 5980.00, '2025-12-22 16:47:57', '采购');

-- ----------------------------
-- Triggers structure for table product
-- ----------------------------
DROP TRIGGER IF EXISTS `before_product_insert`;
delimiter ;;
CREATE TRIGGER `before_product_insert` BEFORE INSERT ON `product` FOR EACH ROW BEGIN
    SET NEW.code = CONCAT('P', LPAD((SELECT COALESCE(MAX(id), 0) + 1 FROM product), 5, '0'));
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;
