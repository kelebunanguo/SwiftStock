package com.swiftstock.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
public class Product {
    private Long id;
    private String name;
    private String code;
    private Long categoryId;
    private Category category;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer minStockLevel;
    private String supplier;
    private Integer status;  // 0: 下架, 1: 上架
    private BigDecimal stockValue;  // 库存价值
    private LocalDateTime inTime;   // 入库时间
    private LocalDateTime outTime;  // 出库时间
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
} 