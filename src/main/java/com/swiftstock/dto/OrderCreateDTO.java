package com.swiftstock.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderCreateDTO {
    private String orderNo;
    private String customerName;
    private String customerPhone;
    private String status;
    private String remark;
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        private Long productId;
        private String productName;
        private Integer quantity;
        private Double price;
        private Double amount;
    }
} 