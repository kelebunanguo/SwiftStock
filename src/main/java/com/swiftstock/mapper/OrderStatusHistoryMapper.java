package com.swiftstock.mapper;

import com.swiftstock.entity.OrderStatusHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单状态变更历史Mapper
 */
@Mapper
public interface OrderStatusHistoryMapper {

    /**
     * 插入一条订单状态变更历史记录
     *
     * @param history 状态历史实体
     * @return 受影响的行数
     */
    int insert(OrderStatusHistory history);
    
    /**
     * 查询指定订单的状态变更历史
     *
     * @param orderId 订单 ID
     * @return 状态历史列表
     */
    List<OrderStatusHistory> findByOrderId(@Param("orderId") Long orderId);
}

