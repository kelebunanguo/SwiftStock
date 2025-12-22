package com.swiftstock.dto;

import lombok.Data;

/**
 * AI 补货推荐VO
 */
@Data
public class AiReorderRecommendVO {
    private Long productId;
    private String productName;
    private String productCode;
    private Integer currentStock;
    private Integer minStock;
    private Integer forecastSales7Days;
    private Integer suggestReorderQuantity;
    private String advice;
}


