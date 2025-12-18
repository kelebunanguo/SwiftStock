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
    
    /**
     * 查询指定前缀的最大订单号
     * @param prefix 订单号前缀（如 "ORD20251218"）
     * @return 最大订单号，如果没有则返回null
     */
    String selectMaxOrderNoByPrefix(@Param("prefix") String prefix);
} 