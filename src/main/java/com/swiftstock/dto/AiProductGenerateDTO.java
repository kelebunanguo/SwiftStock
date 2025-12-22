package com.swiftstock.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO：AI 生成商品详情请求
 */
@Data
@NoArgsConstructor
public class AiProductGenerateDTO {
    @NotBlank(message = "productName 不能为空")
    private String productName;
    private String supplier;
    private String category;
}
