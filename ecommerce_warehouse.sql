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

 Date: 19/12/2025 00:35:58
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
  `level` int NOT NULL DEFAULT 1,
  `sort_order` int NULL DEFAULT 0,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` int NOT NULL DEFAULT 1,
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `parent_id`(`parent_id` ASC) USING BTREE,
  CONSTRAINT `category_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `category` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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
INSERT INTO `category` VALUES (11, '外套', 2, 3, 4, '男士外套', 1, '2025-12-18 17:21:25', '2025-12-18 17:21:25');

-- ----------------------------
-- Table structure for inventory_record
-- ----------------------------
DROP TABLE IF EXISTS `inventory_record`;
CREATE TABLE `inventory_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'IN-入库，OUT-出库',
  `quantity` int NOT NULL,
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `operator_id` bigint NULL DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  INDEX `operator_id`(`operator_id` ASC) USING BTREE,
  CONSTRAINT `inventory_record_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `inventory_record_ibfk_2` FOREIGN KEY (`operator_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of inventory_record
-- ----------------------------

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  `product_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `quantity` int NOT NULL,
  `price` decimal(10, 2) NOT NULL,
  `amount` decimal(10, 2) NOT NULL,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `order_id`(`order_id` ASC) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  CONSTRAINT `order_item_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `order_item_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

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

-- ----------------------------
-- Table structure for order_status_history
-- ----------------------------
DROP TABLE IF EXISTS `order_status_history`;
CREATE TABLE `order_status_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `from_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `to_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `operator_id` bigint NULL DEFAULT NULL,
  `changed_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  CONSTRAINT `fk_order_status_history_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 73 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_status_history
-- ----------------------------
INSERT INTO `order_status_history` VALUES (1, 1, NULL, 'UNPAID', '订单创建', 1, '2025-12-15 09:30:00');
INSERT INTO `order_status_history` VALUES (2, 2, NULL, 'UNPAID', '订单创建', 1, '2025-12-15 10:15:00');
INSERT INTO `order_status_history` VALUES (3, 3, NULL, 'UNPAID', '订单创建', 1, '2025-12-15 11:00:00');
INSERT INTO `order_status_history` VALUES (4, 4, NULL, 'UNPAID', '订单创建', 1, '2025-12-14 14:20:00');
INSERT INTO `order_status_history` VALUES (5, 4, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-15 08:30:00');
INSERT INTO `order_status_history` VALUES (6, 5, NULL, 'UNPAID', '订单创建', 1, '2025-12-14 15:45:00');
INSERT INTO `order_status_history` VALUES (7, 5, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-15 09:00:00');
INSERT INTO `order_status_history` VALUES (8, 6, NULL, 'UNPAID', '订单创建', 1, '2025-12-14 16:30:00');
INSERT INTO `order_status_history` VALUES (9, 6, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-15 09:15:00');
INSERT INTO `order_status_history` VALUES (10, 7, NULL, 'UNPAID', '订单创建', 1, '2025-12-14 10:00:00');
INSERT INTO `order_status_history` VALUES (11, 7, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-14 18:00:00');
INSERT INTO `order_status_history` VALUES (12, 7, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-15 08:00:00');
INSERT INTO `order_status_history` VALUES (13, 8, NULL, 'UNPAID', '订单创建', 1, '2025-12-14 11:30:00');
INSERT INTO `order_status_history` VALUES (14, 8, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-14 19:00:00');
INSERT INTO `order_status_history` VALUES (15, 8, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-15 09:30:00');
INSERT INTO `order_status_history` VALUES (16, 9, NULL, 'UNPAID', '订单创建', 1, '2025-12-13 13:45:00');
INSERT INTO `order_status_history` VALUES (17, 9, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-13 20:00:00');
INSERT INTO `order_status_history` VALUES (18, 9, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-14 09:00:00');
INSERT INTO `order_status_history` VALUES (19, 9, 'PREPARING', 'SHIPPED', '顺丰快递已发货', 1, '2025-12-14 14:00:00');
INSERT INTO `order_status_history` VALUES (20, 10, NULL, 'UNPAID', '订单创建', 1, '2025-12-13 15:20:00');
INSERT INTO `order_status_history` VALUES (21, 10, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-13 21:00:00');
INSERT INTO `order_status_history` VALUES (22, 10, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-14 10:00:00');
INSERT INTO `order_status_history` VALUES (23, 10, 'PREPARING', 'SHIPPED', '圆通快递已发货', 1, '2025-12-14 15:30:00');
INSERT INTO `order_status_history` VALUES (24, 11, NULL, 'UNPAID', '订单创建', 1, '2025-12-13 16:00:00');
INSERT INTO `order_status_history` VALUES (25, 11, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-13 22:00:00');
INSERT INTO `order_status_history` VALUES (26, 11, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-14 11:00:00');
INSERT INTO `order_status_history` VALUES (27, 11, 'PREPARING', 'SHIPPED', '京东快递已发货', 1, '2025-12-14 16:00:00');
INSERT INTO `order_status_history` VALUES (28, 12, NULL, 'UNPAID', '订单创建', 1, '2025-12-10 10:30:00');
INSERT INTO `order_status_history` VALUES (29, 12, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-10 16:00:00');
INSERT INTO `order_status_history` VALUES (30, 12, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-11 09:00:00');
INSERT INTO `order_status_history` VALUES (31, 12, 'PREPARING', 'SHIPPED', '已发货', 1, '2025-12-11 14:00:00');
INSERT INTO `order_status_history` VALUES (32, 12, 'SHIPPED', 'DELIVERED', '客户已签收', 1, '2025-12-12 17:00:00');
INSERT INTO `order_status_history` VALUES (33, 13, NULL, 'UNPAID', '订单创建', 1, '2025-12-10 14:00:00');
INSERT INTO `order_status_history` VALUES (34, 13, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-10 18:00:00');
INSERT INTO `order_status_history` VALUES (35, 13, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-11 10:00:00');
INSERT INTO `order_status_history` VALUES (36, 13, 'PREPARING', 'SHIPPED', '已发货', 1, '2025-12-11 15:00:00');
INSERT INTO `order_status_history` VALUES (37, 13, 'SHIPPED', 'DELIVERED', '客户已签收', 1, '2025-12-12 18:20:00');
INSERT INTO `order_status_history` VALUES (38, 14, NULL, 'UNPAID', '订单创建', 1, '2025-12-11 09:30:00');
INSERT INTO `order_status_history` VALUES (39, 14, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-11 15:00:00');
INSERT INTO `order_status_history` VALUES (40, 14, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-12 08:00:00');
INSERT INTO `order_status_history` VALUES (41, 14, 'PREPARING', 'SHIPPED', '已发货', 1, '2025-12-12 13:00:00');
INSERT INTO `order_status_history` VALUES (42, 14, 'SHIPPED', 'DELIVERED', '客户已签收', 1, '2025-12-13 10:00:00');
INSERT INTO `order_status_history` VALUES (43, 15, NULL, 'UNPAID', '订单创建', 1, '2025-12-11 11:00:00');
INSERT INTO `order_status_history` VALUES (44, 15, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-11 16:00:00');
INSERT INTO `order_status_history` VALUES (45, 15, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-12 09:00:00');
INSERT INTO `order_status_history` VALUES (46, 15, 'PREPARING', 'SHIPPED', '已发货', 1, '2025-12-12 14:00:00');
INSERT INTO `order_status_history` VALUES (47, 15, 'SHIPPED', 'DELIVERED', '客户已签收', 1, '2025-12-13 14:30:00');
INSERT INTO `order_status_history` VALUES (48, 16, NULL, 'UNPAID', '订单创建', 1, '2025-12-08 10:00:00');
INSERT INTO `order_status_history` VALUES (49, 16, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-08 15:00:00');
INSERT INTO `order_status_history` VALUES (50, 16, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-09 09:00:00');
INSERT INTO `order_status_history` VALUES (51, 16, 'PREPARING', 'SHIPPED', '已发货', 1, '2025-12-09 14:00:00');
INSERT INTO `order_status_history` VALUES (52, 16, 'SHIPPED', 'DELIVERED', '客户已签收', 1, '2025-12-11 10:00:00');
INSERT INTO `order_status_history` VALUES (53, 16, 'DELIVERED', 'COMPLETED', '订单已完成', 1, '2025-12-15 10:00:00');
INSERT INTO `order_status_history` VALUES (54, 17, NULL, 'UNPAID', '订单创建', 1, '2025-12-09 11:30:00');
INSERT INTO `order_status_history` VALUES (55, 17, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-09 16:00:00');
INSERT INTO `order_status_history` VALUES (56, 17, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-10 09:00:00');
INSERT INTO `order_status_history` VALUES (57, 17, 'PREPARING', 'SHIPPED', '已发货', 1, '2025-12-10 14:00:00');
INSERT INTO `order_status_history` VALUES (58, 17, 'SHIPPED', 'DELIVERED', '客户已签收', 1, '2025-12-12 11:00:00');
INSERT INTO `order_status_history` VALUES (59, 17, 'DELIVERED', 'COMPLETED', '订单已完成', 1, '2025-12-15 11:30:00');
INSERT INTO `order_status_history` VALUES (60, 18, NULL, 'UNPAID', '订单创建', 1, '2025-12-14 13:00:00');
INSERT INTO `order_status_history` VALUES (61, 18, 'UNPAID', 'CANCELLED', '客户申请取消', 1, '2025-12-14 18:00:00');
INSERT INTO `order_status_history` VALUES (62, 19, NULL, 'UNPAID', '订单创建', 1, '2025-12-14 14:00:00');
INSERT INTO `order_status_history` VALUES (63, 19, 'UNPAID', 'CANCELLED', '库存不足，无法发货', 1, '2025-12-14 19:00:00');
INSERT INTO `order_status_history` VALUES (64, 20, NULL, 'UNPAID', '订单创建', 1, '2025-12-08 14:00:00');
INSERT INTO `order_status_history` VALUES (65, 20, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-08 18:00:00');
INSERT INTO `order_status_history` VALUES (66, 20, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-09 09:00:00');
INSERT INTO `order_status_history` VALUES (67, 20, 'PREPARING', 'SHIPPED', '已发货', 1, '2025-12-09 14:00:00');
INSERT INTO `order_status_history` VALUES (68, 20, 'SHIPPED', 'DELIVERED', '客户已签收', 1, '2025-12-11 15:00:00');
INSERT INTO `order_status_history` VALUES (69, 20, 'DELIVERED', 'REFUNDED', '商品有质量问题，已退货退款', 1, '2025-12-16 10:00:00');
INSERT INTO `order_status_history` VALUES (70, 21, NULL, 'PAID', '订单创建', NULL, '2025-12-18 19:01:18');
INSERT INTO `order_status_history` VALUES (71, 22, NULL, 'UNPAID', '订单创建', NULL, '2025-12-18 19:03:45');
INSERT INTO `order_status_history` VALUES (72, 22, 'UNPAID', 'PAID', '客户付款', NULL, '2025-12-18 19:03:56');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `customer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `customer_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `total_amount` decimal(10, 2) NOT NULL DEFAULT 0.00,
  `operator_id` bigint NULL DEFAULT NULL,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `operator_id`(`operator_id` ASC) USING BTREE,
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`operator_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, 'ORD202512180001', '张伟', '13800001001', 'UNPAID', 398.00, 1, '客户要求确认后立即发货', '2025-12-15 09:30:00', '2025-12-15 09:30:00');
INSERT INTO `orders` VALUES (2, 'ORD202512180002', '李娜娜', '13800001002', 'UNPAID', 598.00, 1, '新客户首单', '2025-12-15 10:15:00', '2025-12-15 10:15:00');
INSERT INTO `orders` VALUES (3, 'ORD202512180003', '王小强', '13800001003', 'UNPAID', 1299.00, 1, '', '2025-12-15 11:00:00', '2025-12-15 11:00:00');
INSERT INTO `orders` VALUES (4, 'ORD202512180004', '刘贵芳', '13800001004', 'PAID', 899.00, 1, '客户要求加急处理', '2025-12-14 14:20:00', '2025-12-15 08:30:00');
INSERT INTO `orders` VALUES (5, 'ORD202512180005', '陈嘉明', '13800001005', 'PAID', 678.00, 1, 'VIP客户', '2025-12-14 15:45:00', '2025-12-15 09:00:00');
INSERT INTO `orders` VALUES (6, 'ORD202512180006', '赵美丽', '13800001006', 'PAID', 1098.00, 1, '', '2025-12-14 16:30:00', '2025-12-15 09:15:00');
INSERT INTO `orders` VALUES (7, 'ORD202512180007', '黄安磊', '13800001007', 'PREPARING', 478.00, 1, '正在配货，预计今日发货', '2025-12-16 10:00:00', '2025-12-17 08:00:00');
INSERT INTO `orders` VALUES (8, 'ORD202512180008', '周杰', '13800001008', 'PREPARING', 798.00, 1, '正在拣货中', '2025-12-16 11:30:00', '2025-12-17 09:30:00');
INSERT INTO `orders` VALUES (9, 'ORD202512180009', '吴言祖', '13800001009', 'SHIPPED', 1697.00, 1, '快递单号：SF1234567890', '2025-12-13 13:45:00', '2025-12-14 14:00:00');
INSERT INTO `orders` VALUES (10, 'ORD202512180010', '郑秀文', '13800001010', 'SHIPPED', 598.00, 1, '快递单号：YTO9876543210', '2025-12-13 15:20:00', '2025-12-14 15:30:00');
INSERT INTO `orders` VALUES (11, 'ORD202512180011', '孙晓梅', '13800001011', 'SHIPPED', 598.00, 1, '快递单号：JD2345678901', '2025-12-13 16:00:00', '2025-12-14 16:00:00');
INSERT INTO `orders` VALUES (12, 'ORD202512180012', '马六六', '13800001012', 'DELIVERED', 898.00, 1, '客户已签收，五星好评', '2025-12-12 10:30:00', '2025-12-14 17:00:00');
INSERT INTO `orders` VALUES (13, 'ORD202512180013', '林青霞', '13800001013', 'DELIVERED', 1498.00, 1, '客户确认收货', '2025-12-12 14:00:00', '2025-12-14 18:20:00');
INSERT INTO `orders` VALUES (14, 'ORD202512180014', '胡嘉清', '13800001014', 'DELIVERED', 698.00, 1, '老客户回购', '2025-12-13 09:30:00', '2025-12-15 10:00:00');
INSERT INTO `orders` VALUES (15, 'ORD202512180015', '徐嘉铭', '13800001015', 'DELIVERED', 1198.00, 1, '企业采购', '2025-12-13 11:00:00', '2025-12-15 14:30:00');
INSERT INTO `orders` VALUES (16, 'ORD202512180016', '高圆园', '13800001016', 'COMPLETED', 999.00, 1, '订单已完成，客户好评', '2025-12-14 10:00:00', '2025-12-17 10:00:00');
INSERT INTO `orders` VALUES (17, 'ORD202512180017', '邓爽', '13800001017', 'COMPLETED', 1599.00, 1, '订单已完成', '2025-12-15 11:30:00', '2025-12-17 11:30:00');
INSERT INTO `orders` VALUES (18, 'ORD202512180018', '孙百川', '13800001018', 'CANCELLED', 299.00, 1, '客户申请取消', '2025-12-17 13:00:00', '2025-12-17 18:00:00');
INSERT INTO `orders` VALUES (19, 'ORD202512180019', '范冰', '13800001019', 'CANCELLED', 399.00, 1, '库存不足，无法发货', '2025-12-17 14:00:00', '2025-12-17 19:00:00');
INSERT INTO `orders` VALUES (20, 'ORD202512180020', '李冰', '13800001020', 'REFUNDED', 899.00, 1, '商品有质量问题，已退货退款', '2025-12-16 14:00:00', '2025-12-18 10:00:00');
INSERT INTO `orders` VALUES (21, 'ORD202512180021', '测试大单客户', '13800138001', 'PAID', 11940.00, 1, NULL, '2025-12-18 19:01:17', '2025-12-19 00:32:24');
INSERT INTO `orders` VALUES (22, 'ORD202512180022', '薛之谦粉丝', '13800138002', 'PAID', 1196.00, NULL, NULL, '2025-12-18 19:03:45', '2025-12-19 00:32:26');

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `category_id` bigint NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `price` decimal(10, 2) NOT NULL,
  `stock_quantity` int NOT NULL DEFAULT 0,
  `min_stock_level` int NOT NULL DEFAULT 0,
  `supplier` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `location` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` int NOT NULL DEFAULT 1,
  `stock_value` decimal(10, 2) NULL DEFAULT 0.00,
  `in_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `out_time` timestamp NULL DEFAULT NULL,
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `code`(`code` ASC) USING BTREE,
  INDEX `category_id`(`category_id` ASC) USING BTREE,
  CONSTRAINT `product_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES (1, 'Nike经典运动短袖', 'P00001', 4, 'Nike经典款运动短袖，100%纯棉材质，吸汗透气', 199.00, 0, 10, 'Nike官方旗舰店', 'A区-1货架', 1, 15920.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 19:01:44');
INSERT INTO `product` VALUES (2, 'Adidas三叶草短袖', 'P00002', 4, 'Adidas经典三叶草logo短袖，时尚百搭', 179.00, 65, 10, 'Adidas官方旗舰店', 'A区-1货架', 1, 11635.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:08');
INSERT INTO `product` VALUES (3, 'Puma彪马运动短袖', 'P00003', 4, 'Puma运动短袖，舒适透气，运动首选', 159.00, 50, 8, 'Puma官方旗舰店', 'A区-2货架', 1, 7950.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:11');
INSERT INTO `product` VALUES (4, 'Under Armour训练短袖', 'P00004', 4, 'UA专业训练短袖，速干面料', 229.00, 45, 8, 'UA官方旗舰店', 'A区-2货架', 1, 10305.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:18');
INSERT INTO `product` VALUES (5, 'Levi\'s经典牛仔裤', 'P00005', 5, 'Levi\'s 501经典直筒牛仔裤，永不过时', 399.00, 55, 8, 'Levi\'s官方旗舰店', 'A区-3货架', 1, 21945.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:21');
INSERT INTO `product` VALUES (6, 'Uniqlo休闲裤', 'P00006', 5, '优衣库舒适休闲裤，四季可穿', 199.00, 75, 10, 'Uniqlo官方旗舰店', 'A区-3货架', 1, 14925.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:24');
INSERT INTO `product` VALUES (7, 'Jack&Jones修身西裤', 'P00007', 5, 'JJ商务修身西裤，职场首选', 349.00, 40, 5, 'JJ官方旗舰店', 'A区-4货架', 1, 13960.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:28');
INSERT INTO `product` VALUES (8, 'Nike运动长裤', 'P00008', 5, 'Nike专业运动长裤，弹力面料', 279.00, 60, 8, 'Nike官方旗舰店', 'A区-4货架', 1, 16740.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:30');
INSERT INTO `product` VALUES (9, 'Nike Air Max 270', 'P00009', 6, 'Nike Air Max 270气垫运动鞋，时尚舒适', 899.00, 35, 5, 'Nike官方旗舰店', 'B区-1货架', 1, 31465.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:33');
INSERT INTO `product` VALUES (10, 'Adidas Ultra Boost', 'P00010', 6, 'Adidas Ultra Boost爆米花跑鞋，缓震极佳', 1299.00, 25, 5, 'Adidas官方旗舰店', 'B区-1货架', 1, 32475.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:36');
INSERT INTO `product` VALUES (11, 'Converse经典帆布鞋', 'P00011', 6, 'Converse All Star经典帆布鞋，百搭神器', 299.00, 90, 15, 'Converse官方旗舰店', 'B区-2货架', 1, 26910.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:38');
INSERT INTO `product` VALUES (12, 'New Balance 574', 'P00012', 6, 'New Balance 574复古跑鞋，经典配色', 599.00, 45, 8, 'NB官方旗舰店', 'B区-2货架', 1, 26955.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:39');
INSERT INTO `product` VALUES (13, 'Nike风衣外套', 'P00013', 11, 'Nike防风运动外套，轻薄便携', 499.00, 50, 8, 'Nike官方旗舰店', 'A区-5货架', 1, 24950.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:42');
INSERT INTO `product` VALUES (14, 'Adidas连帽卫衣', 'P00014', 11, 'Adidas经典连帽卫衣，保暖舒适', 359.00, 65, 10, 'Adidas官方旗舰店', 'A区-5货架', 1, 23335.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:44');
INSERT INTO `product` VALUES (15, 'The North Face冲锋衣', 'P00015', 11, 'TNF专业户外冲锋衣，防水透气', 1299.00, 30, 5, 'TNF官方旗舰店', 'A区-6货架', 1, 38970.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:47');
INSERT INTO `product` VALUES (16, 'Zara基础短袖', 'P00016', 7, 'Zara基础款短袖，简约百搭', 99.00, 100, 15, 'Zara官方旗舰店', 'C区-1货架', 1, 9900.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:50');
INSERT INTO `product` VALUES (17, 'H&M印花短袖', 'P00017', 7, 'H&M时尚印花短袖，青春活力', 79.00, 85, 12, 'H&M官方旗舰店', 'C区-1货架', 1, 6715.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:53');
INSERT INTO `product` VALUES (18, 'Zara雪纺长裙', 'P00018', 8, 'Zara优雅雪纺长裙，仙气飘飘', 399.00, 40, 8, 'Zara官方旗舰店', 'C区-2货架', 1, 15960.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:55');
INSERT INTO `product` VALUES (19, '优衣库棉麻长裙', 'P00019', 8, '优衣库舒适棉麻长裙，夏季必备', 299.00, 55, 10, 'Uniqlo官方旗舰店', 'C区-2货架', 1, 16445.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:31:57');
INSERT INTO `product` VALUES (20, 'Zara复古连衣裙', 'P00020', 10, 'Zara复古风连衣裙，气质优雅', 599.00, 35, 5, 'Zara官方旗舰店', 'C区-3货架', 1, 20965.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:32:04');
INSERT INTO `product` VALUES (21, 'DSP薛之谦同款外套', 'P00021', 11, 'DSP薛之谦同款潮流外套', 299.00, 96, 10, 'DSP官方', 'A区-6货架', 1, 28704.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-19 00:32:08');

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
