package com.swiftstock.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for SupplyRecord requests.
 * <p>
 * 用于接收前端的 JSON，请求中的 receiveTime 使用字符串（yyyy-MM-dd HH:mm:ss）
 * 在控制器中将其转换为 {@link java.time.LocalDateTime}。
 */
@Data
public class SupplyRecordDTO {
    private Long id;
    private Long supplierId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String receiveTime;
    private String remark;
}


