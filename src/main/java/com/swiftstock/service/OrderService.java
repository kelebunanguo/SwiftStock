package com.swiftstock.service;

import com.swiftstock.dto.OrderCreateDTO;
import com.swiftstock.entity.Order;
import com.swiftstock.entity.OrderStatusHistory;
import java.util.List;

/**
 * 订单服务接口
 * - 封装订单相关业务操作（创建、查询、状态变更、取消、删除等），供控制器和其它服务调用。
 */

public interface OrderService {
    Order createOrder(OrderCreateDTO orderDTO);
    List<Order> findAll();
    List<Order> findByCondition(Order searchParams);
    Order findById(Long id);
    void updateStatus(Long id, String status);
    void transitionStatus(Long id, String targetStatus, String reason);
    void cancelOrder(Long id, String reason);
    void deleteById(Long id);
    List<OrderStatusHistory> getStatusHistory(Long id);
} 