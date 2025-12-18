package com.swiftstock.controller;

import com.swiftstock.entity.Product;
import com.swiftstock.service.StockAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存预警控制器
 *
 * <p>预警规则（与 Service 保持一致）：
 * <ul>
 *   <li>低库存：0 < stock_quantity <= min_stock_level</li>
 *   <li>缺货：stock_quantity == 0</li>
 * </ul>
 *
 * <p>用途：为前端预警组件/仪表盘提供数据支撑。
 */
@RestController
@RequestMapping("/stock-alert")
public class StockAlertController {
    
    private static final Logger logger = LoggerFactory.getLogger(StockAlertController.class);
    
    @Autowired
    private StockAlertService stockAlertService;
    
    /**
     * 获取库存预警信息
     *
     * <p>返回：低库存商品列表 + 低库存数量 + 缺货数量 + 总预警数量。
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getStockAlertInfo() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("获取库存预警信息");
            
            List<Product> lowStockProducts = stockAlertService.checkAllStockAlerts();
            int lowStockCount = stockAlertService.getLowStockCount();
            int outOfStockCount = stockAlertService.getOutOfStockCount();
            
            Map<String, Object> data = new HashMap<>();
            data.put("lowStockProducts", lowStockProducts);
            data.put("lowStockCount", lowStockCount);
            data.put("outOfStockCount", outOfStockCount);
            data.put("totalAlertCount", lowStockCount + outOfStockCount);
            
            response.put("success", true);
            response.put("data", data);
            
            logger.info("库存预警信息获取成功：库存不足{}个，缺货{}个", lowStockCount, outOfStockCount);
        } catch (Exception e) {
            logger.error("获取库存预警信息失败", e);
            response.put("success", false);
            response.put("message", "获取库存预警信息失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 检查指定商品的库存预警状态
     *
     * <p>返回 isAlert：是否触发预警（包含低库存与缺货）。
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Object>> checkProductStockAlert(@PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("检查商品{}的库存预警状态", productId);
            
            boolean isAlert = stockAlertService.checkStockAlert(productId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("productId", productId);
            data.put("isAlert", isAlert);
            
            response.put("success", true);
            response.put("data", data);
            
            logger.info("商品{}库存预警检查完成，预警状态：{}", productId, isAlert);
        } catch (Exception e) {
            logger.error("检查商品{}库存预警状态失败", productId, e);
            response.put("success", false);
            response.put("message", "检查库存预警状态失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取库存预警统计
     *
     * <p>用于仪表盘快捷展示：低库存数、缺货数、总预警数。
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStockAlertStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("获取库存预警统计");
            
            int lowStockCount = stockAlertService.getLowStockCount();
            int outOfStockCount = stockAlertService.getOutOfStockCount();
            
            Map<String, Object> data = new HashMap<>();
            data.put("lowStockCount", lowStockCount);
            data.put("outOfStockCount", outOfStockCount);
            data.put("totalAlertCount", lowStockCount + outOfStockCount);
            
            response.put("success", true);
            response.put("data", data);
            
            logger.info("库存预警统计获取成功：库存不足{}个，缺货{}个", lowStockCount, outOfStockCount);
        } catch (Exception e) {
            logger.error("获取库存预警统计失败", e);
            response.put("success", false);
            response.put("message", "获取库存预警统计失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
