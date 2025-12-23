package com.swiftstock.controller;

import com.swiftstock.service.SalesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页销售统计接口（Controller）
 *
 * <p>面向“经营分析”的数据接口，供前端折线图/概览卡片使用。
 * <p>数据来源：基于订单（PAID/COMPLETED）聚合计算（见 {@code SalesServiceImpl}）。
 */
@Slf4j
@RestController
@RequestMapping("/sales")
public class SalesController {

    @Autowired
    private SalesService salesService;

    /**
     * 获取销售趋势数据
     *
     * <p>period 约定：
     * <ul>
     *   <li>7d：近 7 天</li>
     *   <li>30d：近 30 天</li>
     *   <li>90d：近 90 天</li>
     * </ul>
     */
    @GetMapping("/trend")
    public ResponseEntity<Map<String, Object>> getSalesTrend(
            @RequestParam(defaultValue = "7d") String period) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.debug("Getting sales trend for period: {}", period);
            
            List<Map<String, Object>> trendData = salesService.getSalesTrend(period);
            
            response.put("success", true);
            response.put("data", trendData);
        } catch (Exception e) {
            log.error("Failed to get sales trend", e);
            response.put("success", false);
            response.put("message", "获取销售趋势失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取销售统计概览
     *
     * <p>典型指标：总销售额、今日销售额、本月销售额、订单数量等。
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getSalesOverview() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> overview = salesService.getSalesOverview();
            
            response.put("success", true);
            response.put("data", overview);
        } catch (Exception e) {
            log.error("Failed to get sales overview", e);
            response.put("success", false);
            response.put("message", "获取销售概览失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
