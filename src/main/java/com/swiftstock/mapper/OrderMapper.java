package com.swiftstock.mapper;

import com.swiftstock.entity.Order;
import com.swiftstock.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单Mapper
 */
@Mapper
public interface OrderMapper {
    /**
     * 插入订单主表（同时会设置生成的 ID）
     *
     * @param order 订单实体
     */
    void insert(Order order);

    /**
     * 查询所有订单（无条件）
     *
     * @return 订单列表
     */
    List<Order> selectAll();

    /**
     * 根据 ID 查询订单
     *
     * @param id 订单 ID
     * @return 订单实体或 null
     */
    Order selectById(@Param("id") Long id);

    /**
     * 更新订单状态（只更新状态字段）
     *
     * @param order 包含 id 与新 status 的订单实体
     */
    void updateStatus(Order order);
    
    /**
     * 根据 ID 删除订单
     *
     * @param id 订单 ID
     */
    void deleteById(@Param("id") Long id);

    /**
     * 按条件查询订单（支持订单号、客户名、状态、日期范围等）
     *
     * @param condition 查询条件封装的 Order
     * @return 订单列表
     */
    List<Order> findByCondition(Order condition);

    /**
     * 查询指定订单的订单项（用于展示或导出）
     *
     * @param orderId 订单 ID
     * @return 订单项列表
     */
    List<OrderItem> findItemsByOrderId(Long orderId);

    /**
     * 插入订单项（用于在 Service 层批量插入）
     *
     * @param item 订单项实体
     * @return 受影响的行数
     */
    int insertItem(OrderItem item);

    /**
     * 更新订单（整体更新，按 Mapper 定义的字段）
     *
     * @param order 订单实体
     * @return 受影响的行数
     */
    int update(Order order);
    
    /**
     * 查询指定前缀的最大订单号
     * @param prefix 订单号前缀（如 "ORD20251218"）
     * @return 最大订单号，如果没有则返回null
     */
    String selectMaxOrderNoByPrefix(@Param("prefix") String prefix);
} 