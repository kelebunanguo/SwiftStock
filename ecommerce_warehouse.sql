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

 Date: 18/12/2025 21:09:47
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
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

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
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

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
  `quantity` int NOT NULL COMMENT '数量',
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作原因',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作员ID',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `product_id`(`product_id` ASC) USING BTREE,
  INDEX `operator_id`(`operator_id` ASC) USING BTREE,
  CONSTRAINT `inventory_record_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `inventory_record_ibfk_2` FOREIGN KEY (`operator_id`) REFERENCES `admin` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;



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
) ENGINE = InnoDB AUTO_INCREMENT = 111 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item` VALUES (80, 1, 1, 'Nike经典运动短袖', 2, 199.00, 398.00, '蓝色L码', '2025-12-15 09:30:00');
INSERT INTO `order_item` VALUES (81, 2, 18, 'Zara雪纺长裙', 1, 399.00, 399.00, '白色M码', '2025-12-15 10:15:00');
INSERT INTO `order_item` VALUES (82, 2, 17, 'H&M印花短袖', 2, 79.00, 158.00, '粉色S码', '2025-12-15 10:15:00');
INSERT INTO `order_item` VALUES (83, 3, 10, 'Adidas Ultra Boost', 1, 1299.00, 1299.00, '黑白配色 42码', '2025-12-15 11:00:00');
INSERT INTO `order_item` VALUES (84, 4, 9, 'Nike Air Max 270', 1, 899.00, 899.00, '灰色 43码', '2025-12-14 14:20:00');
INSERT INTO `order_item` VALUES (85, 5, 5, 'Levi\'s经典牛仔裤', 1, 399.00, 399.00, '深蓝色 32码', '2025-12-14 15:45:00');
INSERT INTO `order_item` VALUES (86, 5, 8, 'Nike运动长裤', 1, 279.00, 279.00, '黑色 L码', '2025-12-14 15:45:00');
INSERT INTO `order_item` VALUES (87, 6, 14, 'Adidas连帽卫衣', 2, 359.00, 718.00, '灰色 XL码', '2025-12-14 16:30:00');
INSERT INTO `order_item` VALUES (88, 6, 6, 'Uniqlo休闲裤', 2, 199.00, 398.00, '卡其色 L码', '2025-12-14 16:30:00');
INSERT INTO `order_item` VALUES (89, 7, 3, 'Puma彪马运动短袖', 3, 159.00, 477.00, '白色 M码', '2025-12-16 10:00:00');
INSERT INTO `order_item` VALUES (90, 8, 20, 'Zara复古连衣裙', 1, 599.00, 599.00, '碎花款 M码', '2025-12-16 11:30:00');
INSERT INTO `order_item` VALUES (91, 8, 17, 'H&M印花短袖', 2, 79.00, 158.00, '多色 S码', '2025-12-16 11:30:00');
INSERT INTO `order_item` VALUES (92, 9, 15, 'The North Face冲锋衣', 1, 1299.00, 1299.00, '红色 L码', '2025-12-13 13:45:00');
INSERT INTO `order_item` VALUES (93, 9, 5, 'Levi\'s经典牛仔裤', 1, 399.00, 399.00, '黑色 31码', '2025-12-13 13:45:00');
INSERT INTO `order_item` VALUES (94, 10, 11, 'Converse经典帆布鞋', 2, 299.00, 598.00, '黑色高帮 41码', '2025-12-13 15:20:00');
INSERT INTO `order_item` VALUES (95, 11, 19, '优衣库棉麻长裙', 2, 299.00, 598.00, '米色 M码', '2025-12-13 16:00:00');
INSERT INTO `order_item` VALUES (96, 12, 12, 'New Balance 574', 1, 599.00, 599.00, '灰色 42码', '2025-12-12 10:30:00');
INSERT INTO `order_item` VALUES (97, 12, 2, 'Adidas三叶草短袖', 1, 179.00, 179.00, '黑色 L码', '2025-12-12 10:30:00');
INSERT INTO `order_item` VALUES (98, 13, 13, 'Nike风衣外套', 2, 499.00, 998.00, '军绿色 XL码', '2025-12-12 14:00:00');
INSERT INTO `order_item` VALUES (99, 13, 6, 'Uniqlo休闲裤', 2, 199.00, 398.00, '黑色 L码', '2025-12-12 14:00:00');
INSERT INTO `order_item` VALUES (100, 14, 7, 'Jack&Jones修身西裤', 2, 349.00, 698.00, '藏青色 33码', '2025-12-13 09:30:00');
INSERT INTO `order_item` VALUES (101, 15, 14, 'Adidas连帽卫衣', 2, 359.00, 718.00, '黑色 XXL码', '2025-12-13 11:00:00');
INSERT INTO `order_item` VALUES (102, 15, 4, 'Under Armour训练短袖', 2, 229.00, 458.00, '蓝色 L码', '2025-12-13 11:00:00');
INSERT INTO `order_item` VALUES (103, 16, 13, 'Nike风衣外套', 2, 499.00, 998.00, '黑色 L码', '2025-12-14 10:00:00');
INSERT INTO `order_item` VALUES (104, 17, 10, 'Adidas Ultra Boost', 1, 1299.00, 1299.00, '全黑 43码', '2025-12-15 11:30:00');
INSERT INTO `order_item` VALUES (105, 17, 16, 'Zara基础短袖', 3, 99.00, 297.00, '白色 M码', '2025-12-15 11:30:00');
INSERT INTO `order_item` VALUES (106, 18, 16, 'Zara基础短袖', 3, 99.00, 297.00, '黑色 M码', '2025-12-17 13:00:00');
INSERT INTO `order_item` VALUES (107, 19, 5, 'Levi\'s经典牛仔裤', 1, 399.00, 399.00, '浅蓝色 30码', '2025-12-17 14:00:00');
INSERT INTO `order_item` VALUES (108, 20, 9, 'Nike Air Max 270', 1, 899.00, 899.00, '白色 44码', '2025-12-16 14:00:00');
INSERT INTO `order_item` VALUES (109, 77, 58, 'Nike经典运动短袖', 60, 199.00, 11940.00, NULL, '2025-12-18 19:01:17');
INSERT INTO `order_item` VALUES (110, 78, 78, 'DSP薛之谦同款外套', 4, 299.00, 1196.00, NULL, '2025-12-18 19:03:45');

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
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 207 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_status_history
-- ----------------------------
INSERT INTO `order_status_history` VALUES (135, 1, NULL, 'UNPAID', '订单创建', 1, '2025-12-15 09:30:00');
INSERT INTO `order_status_history` VALUES (136, 2, NULL, 'UNPAID', '订单创建', 1, '2025-12-15 10:15:00');
INSERT INTO `order_status_history` VALUES (137, 3, NULL, 'UNPAID', '订单创建', 1, '2025-12-15 11:00:00');
INSERT INTO `order_status_history` VALUES (138, 4, NULL, 'UNPAID', '订单创建', 1, '2025-12-14 14:20:00');
INSERT INTO `order_status_history` VALUES (139, 4, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-15 08:30:00');
INSERT INTO `order_status_history` VALUES (140, 5, NULL, 'UNPAID', '订单创建', 1, '2025-12-14 15:45:00');
INSERT INTO `order_status_history` VALUES (141, 5, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-15 09:00:00');
INSERT INTO `order_status_history` VALUES (142, 6, NULL, 'UNPAID', '订单创建', 1, '2025-12-14 16:30:00');
INSERT INTO `order_status_history` VALUES (143, 6, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-15 09:15:00');
INSERT INTO `order_status_history` VALUES (144, 7, NULL, 'UNPAID', '订单创建', 1, '2025-12-14 10:00:00');
INSERT INTO `order_status_history` VALUES (145, 7, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-14 18:00:00');
INSERT INTO `order_status_history` VALUES (146, 7, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-15 08:00:00');
INSERT INTO `order_status_history` VALUES (147, 8, NULL, 'UNPAID', '订单创建', 1, '2025-12-14 11:30:00');
INSERT INTO `order_status_history` VALUES (148, 8, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-14 19:00:00');
INSERT INTO `order_status_history` VALUES (149, 8, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-15 09:30:00');
INSERT INTO `order_status_history` VALUES (150, 9, NULL, 'UNPAID', '订单创建', 1, '2025-12-13 13:45:00');
INSERT INTO `order_status_history` VALUES (151, 9, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-13 20:00:00');
INSERT INTO `order_status_history` VALUES (152, 9, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-14 09:00:00');
INSERT INTO `order_status_history` VALUES (153, 9, 'PREPARING', 'SHIPPED', '顺丰快递已发货', 1, '2025-12-14 14:00:00');
INSERT INTO `order_status_history` VALUES (154, 10, NULL, 'UNPAID', '订单创建', 1, '2025-12-13 15:20:00');
INSERT INTO `order_status_history` VALUES (155, 10, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-13 21:00:00');
INSERT INTO `order_status_history` VALUES (156, 10, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-14 10:00:00');
INSERT INTO `order_status_history` VALUES (157, 10, 'PREPARING', 'SHIPPED', '圆通快递已发货', 1, '2025-12-14 15:30:00');
INSERT INTO `order_status_history` VALUES (158, 11, NULL, 'UNPAID', '订单创建', 1, '2025-12-13 16:00:00');
INSERT INTO `order_status_history` VALUES (159, 11, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-13 22:00:00');
INSERT INTO `order_status_history` VALUES (160, 11, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-14 11:00:00');
INSERT INTO `order_status_history` VALUES (161, 11, 'PREPARING', 'SHIPPED', '京东快递已发货', 1, '2025-12-14 16:00:00');
INSERT INTO `order_status_history` VALUES (162, 12, NULL, 'UNPAID', '订单创建', 1, '2025-12-10 10:30:00');
INSERT INTO `order_status_history` VALUES (163, 12, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-10 16:00:00');
INSERT INTO `order_status_history` VALUES (164, 12, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-11 09:00:00');
INSERT INTO `order_status_history` VALUES (165, 12, 'PREPARING', 'SHIPPED', '已发货', 1, '2025-12-11 14:00:00');
INSERT INTO `order_status_history` VALUES (166, 12, 'SHIPPED', 'DELIVERED', '客户已签收', 1, '2025-12-12 17:00:00');
INSERT INTO `order_status_history` VALUES (167, 13, NULL, 'UNPAID', '订单创建', 1, '2025-12-10 14:00:00');
INSERT INTO `order_status_history` VALUES (168, 13, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-10 18:00:00');
INSERT INTO `order_status_history` VALUES (169, 13, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-11 10:00:00');
INSERT INTO `order_status_history` VALUES (170, 13, 'PREPARING', 'SHIPPED', '已发货', 1, '2025-12-11 15:00:00');
INSERT INTO `order_status_history` VALUES (171, 13, 'SHIPPED', 'DELIVERED', '客户已签收', 1, '2025-12-12 18:20:00');
INSERT INTO `order_status_history` VALUES (172, 14, NULL, 'UNPAID', '订单创建', 1, '2025-12-11 09:30:00');
INSERT INTO `order_status_history` VALUES (173, 14, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-11 15:00:00');
INSERT INTO `order_status_history` VALUES (174, 14, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-12 08:00:00');
INSERT INTO `order_status_history` VALUES (175, 14, 'PREPARING', 'SHIPPED', '已发货', 1, '2025-12-12 13:00:00');
INSERT INTO `order_status_history` VALUES (176, 14, 'SHIPPED', 'DELIVERED', '客户已签收', 1, '2025-12-13 10:00:00');
INSERT INTO `order_status_history` VALUES (177, 15, NULL, 'UNPAID', '订单创建', 1, '2025-12-11 11:00:00');
INSERT INTO `order_status_history` VALUES (178, 15, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-11 16:00:00');
INSERT INTO `order_status_history` VALUES (179, 15, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-12 09:00:00');
INSERT INTO `order_status_history` VALUES (180, 15, 'PREPARING', 'SHIPPED', '已发货', 1, '2025-12-12 14:00:00');
INSERT INTO `order_status_history` VALUES (181, 15, 'SHIPPED', 'DELIVERED', '客户已签收', 1, '2025-12-13 14:30:00');
INSERT INTO `order_status_history` VALUES (182, 16, NULL, 'UNPAID', '订单创建', 1, '2025-12-08 10:00:00');
INSERT INTO `order_status_history` VALUES (183, 16, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-08 15:00:00');
INSERT INTO `order_status_history` VALUES (184, 16, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-09 09:00:00');
INSERT INTO `order_status_history` VALUES (185, 16, 'PREPARING', 'SHIPPED', '已发货', 1, '2025-12-09 14:00:00');
INSERT INTO `order_status_history` VALUES (186, 16, 'SHIPPED', 'DELIVERED', '客户已签收', 1, '2025-12-11 10:00:00');
INSERT INTO `order_status_history` VALUES (187, 16, 'DELIVERED', 'COMPLETED', '订单已完成', 1, '2025-12-15 10:00:00');
INSERT INTO `order_status_history` VALUES (188, 17, NULL, 'UNPAID', '订单创建', 1, '2025-12-09 11:30:00');
INSERT INTO `order_status_history` VALUES (189, 17, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-09 16:00:00');
INSERT INTO `order_status_history` VALUES (190, 17, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-10 09:00:00');
INSERT INTO `order_status_history` VALUES (191, 17, 'PREPARING', 'SHIPPED', '已发货', 1, '2025-12-10 14:00:00');
INSERT INTO `order_status_history` VALUES (192, 17, 'SHIPPED', 'DELIVERED', '客户已签收', 1, '2025-12-12 11:00:00');
INSERT INTO `order_status_history` VALUES (193, 17, 'DELIVERED', 'COMPLETED', '订单已完成', 1, '2025-12-15 11:30:00');
INSERT INTO `order_status_history` VALUES (194, 18, NULL, 'UNPAID', '订单创建', 1, '2025-12-14 13:00:00');
INSERT INTO `order_status_history` VALUES (195, 18, 'UNPAID', 'CANCELLED', '客户申请取消', 1, '2025-12-14 18:00:00');
INSERT INTO `order_status_history` VALUES (196, 19, NULL, 'UNPAID', '订单创建', 1, '2025-12-14 14:00:00');
INSERT INTO `order_status_history` VALUES (197, 19, 'UNPAID', 'CANCELLED', '库存不足，无法发货', 1, '2025-12-14 19:00:00');
INSERT INTO `order_status_history` VALUES (198, 20, NULL, 'UNPAID', '订单创建', 1, '2025-12-08 14:00:00');
INSERT INTO `order_status_history` VALUES (199, 20, 'UNPAID', 'PAID', '客户已付款', 1, '2025-12-08 18:00:00');
INSERT INTO `order_status_history` VALUES (200, 20, 'PAID', 'PREPARING', '开始配货', 1, '2025-12-09 09:00:00');
INSERT INTO `order_status_history` VALUES (201, 20, 'PREPARING', 'SHIPPED', '已发货', 1, '2025-12-09 14:00:00');
INSERT INTO `order_status_history` VALUES (202, 20, 'SHIPPED', 'DELIVERED', '客户已签收', 1, '2025-12-11 15:00:00');
INSERT INTO `order_status_history` VALUES (203, 20, 'DELIVERED', 'REFUNDED', '商品有质量问题，已退货退款', 1, '2025-12-16 10:00:00');
INSERT INTO `order_status_history` VALUES (204, 77, NULL, 'PAID', '订单创建', NULL, '2025-12-18 19:01:18');
INSERT INTO `order_status_history` VALUES (205, 78, NULL, 'UNPAID', '订单创建', NULL, '2025-12-18 19:03:45');
INSERT INTO `order_status_history` VALUES (206, 78, 'UNPAID', 'PAID', '客户付款', NULL, '2025-12-18 19:03:56');

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
) ENGINE = InnoDB AUTO_INCREMENT = 79 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (57, 'ORD202512180001', '张伟', '13800001001', 'UNPAID', 398.00, 1, '客户要求确认后立即发货', '2025-12-15 09:30:00', '2025-12-15 09:30:00');
INSERT INTO `orders` VALUES (58, 'ORD202512180002', '李娜娜', '13800001002', 'UNPAID', 598.00, 1, '新客户首单', '2025-12-15 10:15:00', '2025-12-15 10:15:00');
INSERT INTO `orders` VALUES (59, 'ORD202512180003', '王小强', '13800001003', 'UNPAID', 1299.00, 1, '', '2025-12-15 11:00:00', '2025-12-15 11:00:00');
INSERT INTO `orders` VALUES (60, 'ORD202512180004', '刘贵芳', '13800001004', 'PAID', 899.00, 1, '客户要求加急处理', '2025-12-14 14:20:00', '2025-12-15 08:30:00');
INSERT INTO `orders` VALUES (61, 'ORD202512180005', '陈嘉明', '13800001005', 'PAID', 678.00, 1, 'VIP客户', '2025-12-14 15:45:00', '2025-12-15 09:00:00');
INSERT INTO `orders` VALUES (62, 'ORD202512180006', '赵美丽', '13800001006', 'PAID', 1098.00, 1, '', '2025-12-14 16:30:00', '2025-12-15 09:15:00');
INSERT INTO `orders` VALUES (63, 'ORD202512180007', '黄安磊', '13800001007', 'PREPARING', 478.00, 1, '正在配货，预计今日发货', '2025-12-16 10:00:00', '2025-12-17 08:00:00');
INSERT INTO `orders` VALUES (64, 'ORD202512180008', '周杰', '13800001008', 'PREPARING', 798.00, 1, '正在拣货中', '2025-12-16 11:30:00', '2025-12-17 09:30:00');
INSERT INTO `orders` VALUES (65, 'ORD202512180009', '吴言祖', '13800001009', 'SHIPPED', 1697.00, 1, '快递单号：SF1234567890', '2025-12-13 13:45:00', '2025-12-14 14:00:00');
INSERT INTO `orders` VALUES (66, 'ORD202512180010', '郑秀文', '13800001010', 'SHIPPED', 598.00, 1, '快递单号：YTO9876543210', '2025-12-13 15:20:00', '2025-12-14 15:30:00');
INSERT INTO `orders` VALUES (67, 'ORD202512180011', '孙晓梅', '13800001011', 'SHIPPED', 598.00, 1, '快递单号：JD2345678901', '2025-12-13 16:00:00', '2025-12-14 16:00:00');
INSERT INTO `orders` VALUES (68, 'ORD202512180012', '马六六', '13800001012', 'DELIVERED', 898.00, 1, '客户已签收，五星好评', '2025-12-12 10:30:00', '2025-12-14 17:00:00');
INSERT INTO `orders` VALUES (69, 'ORD202512180013', '林青霞', '13800001013', 'DELIVERED', 1498.00, 1, '客户确认收货', '2025-12-12 14:00:00', '2025-12-14 18:20:00');
INSERT INTO `orders` VALUES (70, 'ORD202512180014', '胡嘉清', '13800001014', 'DELIVERED', 698.00, 1, '老客户回购', '2025-12-13 09:30:00', '2025-12-15 10:00:00');
INSERT INTO `orders` VALUES (71, 'ORD202512180015', '徐嘉铭', '13800001015', 'DELIVERED', 1198.00, 1, '企业采购', '2025-12-13 11:00:00', '2025-12-15 14:30:00');
INSERT INTO `orders` VALUES (72, 'ORD202512180016', '高圆园', '13800001016', 'COMPLETED', 999.00, 1, '订单已完成，客户好评', '2025-12-14 10:00:00', '2025-12-17 10:00:00');
INSERT INTO `orders` VALUES (73, 'ORD202512180017', '邓爽', '13800001017', 'COMPLETED', 1599.00, 1, '订单已完成', '2025-12-15 11:30:00', '2025-12-17 11:30:00');
INSERT INTO `orders` VALUES (74, 'ORD202512180018', '孙百川', '13800001018', 'CANCELLED', 299.00, 1, '客户申请取消', '2025-12-17 13:00:00', '2025-12-17 18:00:00');
INSERT INTO `orders` VALUES (75, 'ORD202512180019', '范冰', '13800001019', 'CANCELLED', 399.00, 1, '库存不足，无法发货', '2025-12-17 14:00:00', '2025-12-17 19:00:00');
INSERT INTO `orders` VALUES (76, 'ORD202512180020', '李冰', '13800001020', 'REFUNDED', 899.00, 1, '商品有质量问题，已退货退款', '2025-12-16 14:00:00', '2025-12-18 10:00:00');


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
) ENGINE = InnoDB AUTO_INCREMENT = 79 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES (58, 'Nike经典运动短袖', 'P00001', 4, 'Nike经典款运动短袖，100%纯棉材质，吸汗透气', 199.00, 0, 10, 'Nike官方旗舰店', 'A区-1货架', 1, 15920.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 19:01:44');
INSERT INTO `product` VALUES (59, 'Adidas三叶草短袖', 'P00059', 4, 'Adidas经典三叶草logo短袖，时尚百搭', 179.00, 65, 10, 'Adidas官方旗舰店', 'A区-1货架', 1, 11635.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (60, 'Puma彪马运动短袖', 'P00060', 4, 'Puma运动短袖，舒适透气，运动首选', 159.00, 50, 8, 'Puma官方旗舰店', 'A区-2货架', 1, 7950.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (61, 'Under Armour训练短袖', 'P00061', 4, 'UA专业训练短袖，速干面料', 229.00, 45, 8, 'UA官方旗舰店', 'A区-2货架', 1, 10305.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (62, 'Levi\'s经典牛仔裤', 'P00062', 5, 'Levi\'s 501经典直筒牛仔裤，永不过时', 399.00, 55, 8, 'Levi\'s官方旗舰店', 'A区-3货架', 1, 21945.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (63, 'Uniqlo休闲裤', 'P00063', 5, '优衣库舒适休闲裤，四季可穿', 199.00, 75, 10, 'Uniqlo官方旗舰店', 'A区-3货架', 1, 14925.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (64, 'Jack&Jones修身西裤', 'P00064', 5, 'JJ商务修身西裤，职场首选', 349.00, 40, 5, 'JJ官方旗舰店', 'A区-4货架', 1, 13960.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (65, 'Nike运动长裤', 'P00065', 5, 'Nike专业运动长裤，弹力面料', 279.00, 60, 8, 'Nike官方旗舰店', 'A区-4货架', 1, 16740.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (66, 'Nike Air Max 270', 'P00066', 6, 'Nike Air Max 270气垫运动鞋，时尚舒适', 899.00, 35, 5, 'Nike官方旗舰店', 'B区-1货架', 1, 31465.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (67, 'Adidas Ultra Boost', 'P00067', 6, 'Adidas Ultra Boost爆米花跑鞋，缓震极佳', 1299.00, 25, 5, 'Adidas官方旗舰店', 'B区-1货架', 1, 32475.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (68, 'Converse经典帆布鞋', 'P00068', 6, 'Converse All Star经典帆布鞋，百搭神器', 299.00, 90, 15, 'Converse官方旗舰店', 'B区-2货架', 1, 26910.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (69, 'New Balance 574', 'P00069', 6, 'New Balance 574复古跑鞋，经典配色', 599.00, 45, 8, 'NB官方旗舰店', 'B区-2货架', 1, 26955.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (70, 'Nike风衣外套', 'P00070', 11, 'Nike防风运动外套，轻薄便携', 499.00, 50, 8, 'Nike官方旗舰店', 'A区-5货架', 1, 24950.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (71, 'Adidas连帽卫衣', 'P00071', 11, 'Adidas经典连帽卫衣，保暖舒适', 359.00, 65, 10, 'Adidas官方旗舰店', 'A区-5货架', 1, 23335.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (72, 'The North Face冲锋衣', 'P00072', 11, 'TNF专业户外冲锋衣，防水透气', 1299.00, 30, 5, 'TNF官方旗舰店', 'A区-6货架', 1, 38970.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (73, 'Zara基础短袖', 'P00073', 7, 'Zara基础款短袖，简约百搭', 99.00, 100, 15, 'Zara官方旗舰店', 'C区-1货架', 1, 9900.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (74, 'H&M印花短袖', 'P00074', 7, 'H&M时尚印花短袖，青春活力', 79.00, 85, 12, 'H&M官方旗舰店', 'C区-1货架', 1, 6715.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (75, 'Zara雪纺长裙', 'P00075', 8, 'Zara优雅雪纺长裙，仙气飘飘', 399.00, 40, 8, 'Zara官方旗舰店', 'C区-2货架', 1, 15960.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (76, '优衣库棉麻长裙', 'P00076', 8, '优衣库舒适棉麻长裙，夏季必备', 299.00, 55, 10, 'Uniqlo官方旗舰店', 'C区-2货架', 1, 16445.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');
INSERT INTO `product` VALUES (77, 'Zara复古连衣裙', 'P00077', 10, 'Zara复古风连衣裙，气质优雅', 599.00, 35, 5, 'Zara官方旗舰店', 'C区-3货架', 1, 20965.00, '2025-12-18 18:33:39', NULL, '2025-12-18 18:33:39', '2025-12-18 18:33:39');


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
