package com.swiftstock.controller;

import com.swiftstock.service.SalesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/sales")
public class SalesController {

    @Autowired
    private SalesService salesService;

    /**
     * 获取销售趋势数据
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
