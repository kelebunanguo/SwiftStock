package com.swiftstock.controller;

import com.swiftstock.dto.OrderCreateDTO;
import com.swiftstock.entity.Order;
import com.swiftstock.entity.OrderStatus;
import com.swiftstock.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单管理接口（Controller）
 *
 * <p>职责：
 * <ul>
 *   <li>提供订单的创建、查询、状态更新/流转、取消、删除等 REST API</li>
 *   <li>负责入参接收与基础校验，复杂业务规则下沉至 {@code OrderService}</li>
 *   <li>统一返回结构：{@code {success, message?, data?}}</li>
 * </ul>
 *
 * <p>说明：系统上下文路径为 {@code /swiftstock}，因此完整路径形如 {@code /swiftstock/orders/...}。
 */
@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 获取订单列表
     *
     * <p>支持按订单号、客户名称、订单状态筛选。分页参数为简单内存分页（先查全部再 subList），
     * 适合毕业设计/小数据量演示；如数据量增大建议改为数据库层分页。
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrders(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.debug("Searching orders with params: orderNo={}, customerName={}, status={}", orderNo, customerName, status);
            
            Order searchParams = new Order();
            searchParams.setOrderNo(orderNo);
            searchParams.setCustomerName(customerName);
            if (status != null && !status.isEmpty()) {
                try {
                    // 将字符串状态转换为枚举（例如 "PAID" / "COMPLETED"）
                    searchParams.setStatus(OrderStatus.valueOf(status.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // 如果状态值无效，忽略该参数
                    log.warn("Invalid order status: {}", status);
                }
            }
            
            List<Order> orders = orderService.findByCondition(searchParams);
            log.debug("Found {} orders", orders.size());
            
            // 简单分页处理：对查询结果做 subList 截断
            int start = (page - 1) * size;
            int end = Math.min(start + size, orders.size());
            List<Order> pageOrders = orders.subList(start, end);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", pageOrders);
            data.put("total", orders.size());
            data.put("page", page);
            data.put("size", size);
            
            response.put("success", true);
            response.put("data", data);
        } catch (Exception e) {
            log.error("Failed to get order list", e);
            response.put("success", false);
            response.put("message", "获取订单列表失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取订单详情
     *
     * <p>返回订单基础信息 + 订单项列表（由 Service/Mapper 负责组装）。
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Order order = orderService.findById(id);
            if (order == null) {
                response.put("success", false);
                response.put("message", "订单不存在");
            } else {
                response.put("success", true);
                response.put("data", order);
            }
        } catch (Exception e) {
            log.error("Failed to get order details. Order ID: {}", id, e);
            response.put("success", false);
            response.put("message", "获取订单详情失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 创建订单
     *
     * <p>业务要点：
     * <ul>
     *   <li>写入 {@code orders} 与 {@code order_item}</li>
     *   <li>若订单初始状态为 {@code PAID}，则自动扣减库存（见 {@code OrderServiceImpl#createOrder}）</li>
     * </ul>
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderCreateDTO orderDTO) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Order order = orderService.createOrder(orderDTO);
            response.put("success", true);
            response.put("message", "订单创建成功");
            response.put("data", order);
        } catch (Exception e) {
            log.error("Failed to create order", e);
            response.put("success", false);
            response.put("message", "订单创建失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新订单状态
     *
     * <p>说明：该接口用于“直接设置目标状态”。状态合法性、业务约束、库存扣减/回滚在 Service 内完成。
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String status = request.get("status");
            log.debug("Updating order {} status to {}", id, status);
            
            orderService.updateStatus(id, status.toUpperCase());
            response.put("success", true);
            response.put("message", "订单状态更新成功");
        } catch (Exception e) {
            log.error("Failed to update order status. Order ID: {}, Status: {}", id, request.get("status"), e);
            response.put("success", false);
            response.put("message", "订单状态更新失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 订单状态流转（支持完整流程）
     *
     * <p>与 {@code /status} 的差异：此接口允许携带 {@code reason}（原因）用于日志/审计说明。
     */
    @PutMapping("/{id}/transition")
    public ResponseEntity<Map<String, Object>> transitionStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String targetStatus = request.get("status");
            String reason = request.get("reason");
            log.debug("Transitioning order {} to status {}", id, targetStatus);
            
            orderService.transitionStatus(id, targetStatus.toUpperCase(), reason);
            response.put("success", true);
            response.put("message", "订单状态流转成功");
        } catch (Exception e) {
            log.error("Failed to transition order status. Order ID: {}, Status: {}", id, request.get("status"), e);
            response.put("success", false);
            response.put("message", "订单状态流转失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 取消订单
     *
     * <p>取消的业务约束与库存恢复逻辑由 Service 处理。
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long id, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String reason = request.get("reason");
            log.debug("Cancelling order {} with reason: {}", id, reason);
            
            orderService.cancelOrder(id, reason);
            response.put("success", true);
            response.put("message", "订单取消成功");
        } catch (Exception e) {
            log.error("Failed to cancel order. Order ID: {}", id, e);
            response.put("success", false);
            response.put("message", "订单取消失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取订单状态流转历史
     *
     * <p>说明：当前实现为“根据当前状态推演生成”的演示版本，未单独落库记录每次状态变更的时间点。
     * 论文/答辩可作为“待优化点”：新增状态变更表记录操作人/时间/原因。
     */
    @GetMapping("/{id}/status-history")
    public ResponseEntity<Map<String, Object>> getStatusHistory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("success", true);
            response.put("data", orderService.getStatusHistory(id));
        } catch (Exception e) {
            log.error("Failed to get order status history. Order ID: {}", id, e);
            response.put("success", false);
            response.put("message", "获取状态历史失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 删除订单
     *
     * <p>若订单处于已付款/履约中状态，删除前会先恢复库存，避免库存永久减少（见 Service 实现）。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.debug("Deleting order: {}", id);
            
            orderService.deleteById(id);
            response.put("success", true);
            response.put("message", "订单删除成功");
        } catch (Exception e) {
            log.error("Failed to delete order. Order ID: {}", id, e);
            response.put("success", false);
            response.put("message", "删除订单失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}