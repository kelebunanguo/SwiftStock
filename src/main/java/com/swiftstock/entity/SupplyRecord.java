package com.swiftstock.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 供货记录实体
 */
@Data
public class SupplyRecord {
    private Long id;
    private Long supplierId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private java.math.BigDecimal unitPrice;
    private java.math.BigDecimal totalAmount;
    private LocalDateTime receiveTime;
    private String remark;
}


