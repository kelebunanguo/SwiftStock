package com.swiftstock.service;

import com.swiftstock.dto.OrderCreateDTO;
import com.swiftstock.entity.Order;
import java.util.List;
import java.util.Map;

public interface OrderService {
    Order createOrder(OrderCreateDTO orderDTO);
    List<Order> findAll();
    List<Order> findByCondition(Order searchParams);
    Order findById(Long id);
    void updateStatus(Long id, String status);
    void transitionStatus(Long id, String targetStatus, String reason);
    void cancelOrder(Long id, String reason);
    void deleteById(Long id);
    List<Map<String, Object>> getStatusHistory(Long id);
} 