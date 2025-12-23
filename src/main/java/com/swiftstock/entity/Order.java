package com.swiftstock.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单实体
 */
@Data
public class Order {
    private Long id;
    private String orderNo;
    private String customerName;
    private String customerPhone;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String remark;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private List<OrderItem> items = new ArrayList<>();
    
    // 非数据库字段
    private Integer itemCount;
    private String startDate;
    private String endDate;
    
    public Integer getItemCount() {
        if (items != null) {
            return items.stream()
                    .mapToInt(OrderItem::getQuantity)
                    .sum();
        }
        return 0;
    }
} 