package com.swiftstock.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 供货记录请求 DTO
 *
 * <p>用于接收前端提交的供货记录数据。注意：前端传递的 {@code receiveTime} 为字符串格式（yyyy-MM-dd HH:mm:ss），
 * 控制器会在接收后解析为 {@link java.time.LocalDateTime}。
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


