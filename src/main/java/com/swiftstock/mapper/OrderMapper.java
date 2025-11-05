package com.swiftstock.mapper;

import com.swiftstock.entity.Order;
import com.swiftstock.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    void insert(Order order);
    List<Order> selectAll();
    Order selectById(@Param("id") Long id);
    void updateStatus(Order order);
    
    void deleteById(@Param("id") Long id);
    List<Order> findByCondition(Order condition);
    List<OrderItem> findItemsByOrderId(Long orderId);
    int insertItem(OrderItem item);
    int update(Order order);
} 