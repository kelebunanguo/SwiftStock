package com.swiftstock.controller;

import com.swiftstock.entity.Order;
import com.swiftstock.entity.Product;
import com.swiftstock.service.OrderService;
import com.swiftstock.service.ProductService;
import com.swiftstock.service.StockAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;
    
    @Autowired
    private StockAlertService stockAlertService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取所有商品和订单（不分页）
            List<Product> allProducts = productService.findAll();
            List<Order> allOrders = orderService.findAll();
            
            // 计算统计数据
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProducts", allProducts.size());
            stats.put("totalOrders", allOrders.size());
            stats.put("totalInventory", allProducts.stream()
                .mapToInt(p -> p.getStockQuantity() != null ? p.getStockQuantity() : 0)
                .sum());
            stats.put("lowStockProducts", stockAlertService.getLowStockCount());
            stats.put("outOfStockProducts", stockAlertService.getOutOfStockCount());
            
            response.put("success", true);
            response.put("data", stats);
            
        } catch (Exception e) {
            log.error("Failed to get dashboard stats", e);
            response.put("success", false);
            response.put("message", "获取统计数据失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取最近订单
     */
    @GetMapping("/recent-orders")
    public ResponseEntity<Map<String, Object>> getRecentOrders() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Order> allOrders = orderService.findAll();
            // 按创建时间倒序排列，取前5条
            List<Order> recentOrders = allOrders.stream()
                .sorted((a, b) -> b.getCreatedTime().compareTo(a.getCreatedTime()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());
            
            response.put("success", true);
            response.put("data", recentOrders);
            
        } catch (Exception e) {
            log.error("Failed to get recent orders", e);
            response.put("success", false);
            response.put("message", "获取最近订单失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
} 