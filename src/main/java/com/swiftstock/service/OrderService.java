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
    /**
     * 创建订单（包含订单主表与订单项），并根据状态决定是否同步扣减库存（PAID 时扣减）。
     *
     * @param orderDTO 订单创建 DTO
     * @return 创建后的 Order 实体
     */
    Order createOrder(OrderCreateDTO orderDTO);

    /**
     * 查询所有订单（无条件）
     *
     * @return 订单列表
     */
    List<Order> findAll();

    /**
     * 按条件查询订单
     *
     * @param searchParams 查询条件封装的 Order（支持订单号/客户名/状态/日期范围等）
     * @return 满足条件的订单列表
     */
    List<Order> findByCondition(Order searchParams);

    /**
     * 根据 ID 查询订单
     *
     * @param id 订单 ID
     * @return 订单实体或 null
     */
    Order findById(Long id);

    /**
     * 更新订单状态（直接设置目标状态，Service 层负责业务校验与库存处理）
     *
     * @param id     订单 ID
     * @param status 目标状态（如 PAID、SHIPPED 等）
     */
    void updateStatus(Long id, String status);

    /**
     * 按工作流进行订单状态流转（允许携带原因用于审计）
     *
     * @param id           订单 ID
     * @param targetStatus 目标状态
     * @param reason       操作原因/备注
     */
    void transitionStatus(Long id, String targetStatus, String reason);

    /**
     * 取消订单（根据业务规则恢复库存/记录日志）
     *
     * @param id     订单 ID
     * @param reason 取消原因
     */
    void cancelOrder(Long id, String reason);

    /**
     * 删除订单（若已付款则先恢复库存）
     *
     * @param id 订单 ID
     */
    void deleteById(Long id);

    /**
     * 获取订单状态流转历史
     *
     * @param id 订单 ID
     * @return 状态历史列表
     */
    List<OrderStatusHistory> getStatusHistory(Long id);
} 