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

/**
 * 报表接口（Controller）
 *
 * <p>功能：
 * <ul>
 *   <li>库存报表：统计库存健康度（正常/低库存/缺货）、总库存价值、明细列表</li>
 *   <li>销售报表：当前实现包含演示数据（便于前端图表展示）；销售趋势/概览建议使用 /sales 接口</li>
 * </ul>
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ProductService productService;

    /**
     * 获取库存报表
     *
     * <p>实现方式：
     * <ul>
     *   <li>读取全部商品</li>
     *   <li>按库存与安全阈值进行分类统计</li>
     *   <li>库存价值 = price * stock_quantity</li>
     * </ul>
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
            
            // 计算库存价值（总价值 = Σ(单价 * 库存数量)）
            BigDecimal totalValue = products.stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 库存价值由前端/临时计算，不持久化到数据库，Report 返回时直接计算展示
            
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
     *
     * <p>说明：该接口目前为“演示数据”，便于前端 ECharts 展示。
     * 真实销售统计建议基于订单表（PAID/COMPLETED）按时间范围聚合计算。
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