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

/**
 * 首页接口（Controller）
 *
 * <p>用途：为首页看板提供汇总数据（商品数、订单数、库存总量、预警数量等）。
 * <p>特点：以“聚合统计”为主，不提供复杂写操作。
 */
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
     *
     * <p>统计口径：
     * <ul>
     *   <li>totalProducts：商品总数</li>
     *   <li>totalOrders：订单总数</li>
     *   <li>totalInventory：所有商品库存数量求和</li>
     *   <li>lowStockProducts/outOfStockProducts：来自库存预警服务</li>
     * </ul>
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取所有商品和订单（不分页，适用于演示数据规模）
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
     *
     * <p>按创建时间倒序取前 5 条，用于首页“最近订单”模块展示。
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