package com.swiftstock.mapper;

import com.swiftstock.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 订单项Mapper
 */
@Mapper
public interface OrderItemMapper {
    /**
     * 插入订单项
     *
     * @param item 订单项实体
     */
    void insert(OrderItem item);

    /**
     * 查询指定订单的所有订单项
     *
     * @param orderId 订单 ID
     * @return 订单项列表
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单 ID 删除对应的订单项
     *
     * @param orderId 订单 ID
     */
    void deleteByOrderId(@Param("orderId") Long orderId);

    /**
     * 查询指定商品在指定日期范围内的销量总和
     *
     * @param productId 商品 ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 销量总和
     */
    Integer sumSalesQuantityByProductId(@Param("productId") Long productId, 
                                       @Param("startDate") String startDate, 
                                       @Param("endDate") String endDate);
}


