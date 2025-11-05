package com.swiftstock.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InventoryRecord {
    private Long id;
    private Long productId;
    private String productName;
    private String type;  // IN-入库，OUT-出库
    private Integer quantity;
    private String reason;
    private LocalDateTime createdTime;
} 