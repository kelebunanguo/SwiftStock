package com.swiftstock.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据迁移控制器（临时使用）
 */
@RestController
@RequestMapping("/migration")
public class MigrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(MigrationController.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 更新订单状态映射
     */
    @PostMapping("/update-order-status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("开始更新订单状态映射");
            
            // 更新 PENDING -> UNPAID
            int pendingCount = jdbcTemplate.update("UPDATE orders SET status = 'UNPAID' WHERE status = 'PENDING'");
            logger.info("更新了 {} 个 PENDING 状态为 UNPAID", pendingCount);
            
            // 更新 PROCESSING -> PAID
            int processingCount = jdbcTemplate.update("UPDATE orders SET status = 'PAID' WHERE status = 'PROCESSING'");
            logger.info("更新了 {} 个 PROCESSING 状态为 PAID", processingCount);
            
            // 查询更新后的结果
            Integer totalOrders = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
            
            Map<String, Object> data = new HashMap<>();
            data.put("pendingUpdated", pendingCount);
            data.put("processingUpdated", processingCount);
            data.put("totalOrders", totalOrders);
            
            response.put("success", true);
            response.put("message", "订单状态更新成功");
            response.put("data", data);
            
            logger.info("订单状态更新完成：PENDING->UNPAID: {}, PROCESSING->PAID: {}, 总订单数: {}", 
                       pendingCount, processingCount, totalOrders);
            
        } catch (Exception e) {
            logger.error("更新订单状态失败", e);
            response.put("success", false);
            response.put("message", "更新订单状态失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 查看当前订单状态分布
     */
    @GetMapping("/order-status-stats")
    public ResponseEntity<Map<String, Object>> getOrderStatusStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("查询订单状态分布");
            
            Map<String, Integer> statusStats = new HashMap<>();
            
            // 查询各种状态的数量
            String[] statuses = {"PAID", "UNPAID", "COMPLETED", "CANCELLED", "PENDING", "PROCESSING"};
            for (String status : statuses) {
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM orders WHERE status = ?", 
                    Integer.class, 
                    status
                );
                statusStats.put(status, count);
            }
            
            response.put("success", true);
            response.put("data", statusStats);
            
            logger.info("订单状态分布查询完成：{}", statusStats);
            
        } catch (Exception e) {
            logger.error("查询订单状态分布失败", e);
            response.put("success", false);
            response.put("message", "查询订单状态分布失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
