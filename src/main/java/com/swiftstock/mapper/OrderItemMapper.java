package com.swiftstock.mapper;

import com.swiftstock.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderItemMapper {
    void insert(OrderItem item);
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);
    void deleteByOrderId(@Param("orderId") Long orderId);
} 