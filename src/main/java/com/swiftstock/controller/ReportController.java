package com.swiftstock.controller;

import com.swiftstock.entity.Product;
import com.swiftstock.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ProductService productService;

    /**
     * 获取库存报表
     */
    @GetMapping("/stock")
    public ResponseEntity<Map<String, Object>> getStockReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long categoryId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Product> products = productService.findAll();
            
            // 统计数据
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProducts", products.size());
            
            long normalStock = products.stream()
                .filter(p -> p.getStockQuantity() > p.getMinStockLevel())
                .count();
            stats.put("normalStock", normalStock);
            
            long lowStock = products.stream()
                .filter(p -> p.getStockQuantity() > 0 && p.getStockQuantity() <= p.getMinStockLevel())
                .count();
            stats.put("lowStock", lowStock);
            
            long outOfStock = products.stream()
                .filter(p -> p.getStockQuantity() == 0)
                .count();
            stats.put("outOfStock", outOfStock);
            
            // 计算库存价值
            BigDecimal totalValue = products.stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 为每个商品添加库存价值
            products.forEach(p -> {
                BigDecimal stockValue = p.getPrice().multiply(BigDecimal.valueOf(p.getStockQuantity()));
                p.setStockValue(stockValue);
            });
            
            Map<String, Object> data = new HashMap<>();
            data.put("stats", stats);
            data.put("details", products);
            data.put("total", products.size());
            data.put("totalValue", totalValue);
            
            response.put("success", true);
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取库存报表失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取销售报表
     */
    @GetMapping("/sales")
    public ResponseEntity<Map<String, Object>> getSalesReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 模拟销售数据
            Map<String, Object> data = new HashMap<>();
            data.put("totalSales", 125000.00);
            data.put("totalOrders", 89);
            data.put("averageOrderValue", 1404.49);
            
            // 模拟每日销售数据
            List<Map<String, Object>> dailySales = List.of(
                Map.of("date", "2025-01-01", "sales", 1200.00),
                Map.of("date", "2025-01-02", "sales", 2000.00),
                Map.of("date", "2025-01-03", "sales", 1500.00),
                Map.of("date", "2025-01-04", "sales", 3000.00),
                Map.of("date", "2025-01-05", "sales", 2500.00),
                Map.of("date", "2025-01-06", "sales", 1800.00),
                Map.of("date", "2025-01-07", "sales", 2200.00)
            );
            data.put("dailySales", dailySales);
            
            response.put("success", true);
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取销售报表失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}