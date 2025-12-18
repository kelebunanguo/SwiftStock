package com.swiftstock.mapper;

import com.swiftstock.entity.OrderStatusHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderStatusHistoryMapper {

    int insert(OrderStatusHistory history);

    List<OrderStatusHistory> findByOrderId(@Param("orderId") Long orderId);
}

