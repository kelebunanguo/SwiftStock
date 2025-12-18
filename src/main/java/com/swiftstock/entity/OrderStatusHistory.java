package com.swiftstock.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 订单状态变更历史实体
 *
 * <p>用途：
 * <ul>
 *   <li>记录订单每一次状态流转（含创建、取消、退款等）</li>
 *   <li>支撑前端展示真实的状态时间轴，便于审计和追溯</li>
 * </ul>
 */
@Data
public class OrderStatusHistory {
    private Long id;
    private Long orderId;
    private OrderStatus fromStatus;
    private OrderStatus toStatus;
    private String reason;
    private Long operatorId;
    private LocalDateTime changedTime;
}

