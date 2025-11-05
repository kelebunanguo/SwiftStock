package com.swiftstock.entity;

public enum OrderStatus {
    UNPAID("待付款"),
    PAID("已付款"),
    PREPARING("配货中"),
    SHIPPED("已发货"),
    DELIVERED("已送达"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    REFUNDED("已退款");
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    // 获取状态流转顺序
    public static OrderStatus[] getStatusFlow() {
        return new OrderStatus[]{
            UNPAID, PAID, PREPARING, SHIPPED, DELIVERED, COMPLETED
        };
    }
    
    // 检查状态是否可以流转到目标状态
    public boolean canTransitionTo(OrderStatus targetStatus) {
        OrderStatus[] flow = getStatusFlow();
        int currentIndex = -1;
        int targetIndex = -1;
        
        for (int i = 0; i < flow.length; i++) {
            if (flow[i] == this) currentIndex = i;
            if (flow[i] == targetStatus) targetIndex = i;
        }
        
        // 取消和退款可以从任何状态转换（除了已完成、已取消、已退款）
        if (targetStatus == CANCELLED || targetStatus == REFUNDED) {
            return this != COMPLETED && this != CANCELLED && this != REFUNDED;
        }
        
        // 正常流程只能向前流转
        return currentIndex != -1 && targetIndex != -1 && targetIndex > currentIndex;
    }
    
    // 检查状态是否需要库存验证
    public boolean requiresStockValidation() {
        return this == PREPARING;
    }
    
    // 检查状态是否需要付款验证
    public boolean requiresPaymentValidation() {
        return this == PAID || this == PREPARING || this == SHIPPED || this == DELIVERED || this == COMPLETED;
    }
    
    // 检查状态是否可以取消
    public boolean canBeCancelled() {
        return this != COMPLETED && this != CANCELLED && this != REFUNDED;
    }
    
    // 检查状态是否可以退款
    public boolean canBeRefunded() {
        return this == PAID || this == PREPARING || this == SHIPPED || this == DELIVERED;
    }
    
    // 获取状态的业务含义
    public String getBusinessMeaning() {
        switch (this) {
            case UNPAID:
                return "等待客户付款";
            case PAID:
                return "已收到付款，准备配货";
            case PREPARING:
                return "正在配货，检查库存";
            case SHIPPED:
                return "已发货，等待送达";
            case DELIVERED:
                return "已送达，等待确认";
            case COMPLETED:
                return "订单完成";
            case CANCELLED:
                return "订单已取消";
            case REFUNDED:
                return "订单已退款";
            default:
                return "未知状态";
        }
    }
} 