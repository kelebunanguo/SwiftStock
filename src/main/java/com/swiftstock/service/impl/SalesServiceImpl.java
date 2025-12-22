package com.swiftstock.service.impl;

import com.swiftstock.entity.Order;
import com.swiftstock.entity.OrderStatus;
import com.swiftstock.mapper.OrderMapper;
import com.swiftstock.service.SalesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 销售统计服务实现
 *
 * <p>提供销售趋势与概览数据的计算，基于订单表聚合已付款及后续状态订单的销售额。</p>
 */
@Slf4j
@Service
public class SalesServiceImpl implements SalesService {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 获取销售趋势数据（按天聚合）
     *
     * @param period 时间周期标识，例：7d、30d、90d
     * @return 每日销售额的列表（date, amount）
     */
    @Override
    public List<Map<String, Object>> getSalesTrend(String period) {
        List<Map<String, Object>> trendData = new ArrayList<>();
        
        try {
            // 根据时间段确定查询范围
            LocalDate endDate = LocalDate.now();
            LocalDate startDate;
            String dateFormat;
            
            switch (period) {
                case "7d":
                    startDate = endDate.minusDays(6);
                    dateFormat = "MM-dd";
                    break;
                case "30d":
                    startDate = endDate.minusDays(29);
                    dateFormat = "MM-dd";
                    break;
                case "90d":
                    startDate = endDate.minusDays(89);
                    dateFormat = "MM-dd";
                    break;
                default:
                    startDate = endDate.minusDays(6);
                    dateFormat = "MM-dd";
            }
            
            // 获取订单数据
            List<Order> orders = orderMapper.selectAll();
            
            // 按日期分组统计销售额
            Map<String, BigDecimal> dailySales = new LinkedHashMap<>();
            
            // 初始化所有日期的销售额为0
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                String dateKey = currentDate.format(DateTimeFormatter.ofPattern(dateFormat));
                dailySales.put(dateKey, BigDecimal.ZERO);
                currentDate = currentDate.plusDays(1);
            }
            
            // 统计实际销售额（所有已付款的订单状态：PAID及之后的状态都计入销售额）
            for (Order order : orders) {
                // 统计所有已付款的订单：PAID、PREPARING、SHIPPED、DELIVERED、COMPLETED
                if (order.getStatus() == OrderStatus.PAID || 
                    order.getStatus() == OrderStatus.PREPARING ||
                    order.getStatus() == OrderStatus.SHIPPED ||
                    order.getStatus() == OrderStatus.DELIVERED ||
                    order.getStatus() == OrderStatus.COMPLETED) {
                    LocalDateTime orderTime = order.getCreatedTime();
                    if (orderTime != null) {
                        LocalDate orderDate = orderTime.toLocalDate();
                        if (!orderDate.isBefore(startDate) && !orderDate.isAfter(endDate)) {
                            String dateKey = orderDate.format(DateTimeFormatter.ofPattern(dateFormat));
                            BigDecimal currentAmount = dailySales.get(dateKey);
                            dailySales.put(dateKey, currentAmount.add(order.getTotalAmount()));
                        }
                    }
                }
            }
            
            // 转换为前端需要的格式
            for (Map.Entry<String, BigDecimal> entry : dailySales.entrySet()) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", entry.getKey());
                dayData.put("amount", entry.getValue().doubleValue());
                trendData.add(dayData);
            }
            
            log.debug("Generated sales trend data for period {}: {} days", period, trendData.size());
            
        } catch (Exception e) {
            log.error("Error generating sales trend data", e);
            throw new RuntimeException("生成销售趋势数据失败：" + e.getMessage(), e);
        }
        
        return trendData;
    }

    /**
     * 获取销售概览数据（总销售、本日、本月等指标）
     *
     * @return 包含 totalSales、todaySales、monthSales、totalOrders 等指标的 Map
     */
    @Override
    public Map<String, Object> getSalesOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        try {
            List<Order> orders = orderMapper.selectAll();
            
            // 统计总销售额（所有已付款的订单）
            BigDecimal totalSales = orders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.PAID || 
                                   order.getStatus() == OrderStatus.PREPARING ||
                                   order.getStatus() == OrderStatus.SHIPPED ||
                                   order.getStatus() == OrderStatus.DELIVERED ||
                                   order.getStatus() == OrderStatus.COMPLETED)
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 统计今日销售额
            LocalDate today = LocalDate.now();
            BigDecimal todaySales = orders.stream()
                    .filter(order -> {
                        if (order.getCreatedTime() == null) return false;
                        LocalDate orderDate = order.getCreatedTime().toLocalDate();
                        return orderDate.equals(today) && 
                               (order.getStatus() == OrderStatus.PAID || 
                                order.getStatus() == OrderStatus.PREPARING ||
                                order.getStatus() == OrderStatus.SHIPPED ||
                                order.getStatus() == OrderStatus.DELIVERED ||
                                order.getStatus() == OrderStatus.COMPLETED);
                    })
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 统计本月销售额
            LocalDate monthStart = today.withDayOfMonth(1);
            BigDecimal monthSales = orders.stream()
                    .filter(order -> {
                        if (order.getCreatedTime() == null) return false;
                        LocalDate orderDate = order.getCreatedTime().toLocalDate();
                        return !orderDate.isBefore(monthStart) && 
                               (order.getStatus() == OrderStatus.PAID || 
                                order.getStatus() == OrderStatus.PREPARING ||
                                order.getStatus() == OrderStatus.SHIPPED ||
                                order.getStatus() == OrderStatus.DELIVERED ||
                                order.getStatus() == OrderStatus.COMPLETED);
                    })
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // 统计订单数量（所有已付款的订单）
            long totalOrders = orders.stream()
                    .filter(order -> order.getStatus() == OrderStatus.PAID || 
                                   order.getStatus() == OrderStatus.PREPARING ||
                                   order.getStatus() == OrderStatus.SHIPPED ||
                                   order.getStatus() == OrderStatus.DELIVERED ||
                                   order.getStatus() == OrderStatus.COMPLETED)
                    .count();
            
            long todayOrders = orders.stream()
                    .filter(order -> {
                        if (order.getCreatedTime() == null) return false;
                        LocalDate orderDate = order.getCreatedTime().toLocalDate();
                        return orderDate.equals(today) && 
                               (order.getStatus() == OrderStatus.PAID || 
                                order.getStatus() == OrderStatus.PREPARING ||
                                order.getStatus() == OrderStatus.SHIPPED ||
                                order.getStatus() == OrderStatus.DELIVERED ||
                                order.getStatus() == OrderStatus.COMPLETED);
                    })
                    .count();
            
            overview.put("totalSales", totalSales.doubleValue());
            overview.put("todaySales", todaySales.doubleValue());
            overview.put("monthSales", monthSales.doubleValue());
            overview.put("totalOrders", totalOrders);
            overview.put("todayOrders", todayOrders);
            
            log.debug("Generated sales overview: totalSales={}, todaySales={}, monthSales={}", 
                     totalSales, todaySales, monthSales);
            
        } catch (Exception e) {
            log.error("Error generating sales overview", e);
            throw new RuntimeException("生成销售概览失败：" + e.getMessage(), e);
        }
        
        return overview;
    }
}
