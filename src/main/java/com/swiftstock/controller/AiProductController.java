package com.swiftstock.controller;

import com.swiftstock.dto.AiProductGenerateDTO;
import com.swiftstock.dto.Result;
import com.swiftstock.service.AiProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AI 商品详情生成控制器
 */
@RestController
@RequestMapping("/api/ai/product")
public class AiProductController {

    private final AiProductService aiProductService;

    @Autowired
    public AiProductController(AiProductService aiProductService) {
        this.aiProductService = aiProductService;
    }


    @PostMapping("/generate-detail")
    public ResponseEntity<Result<String>> generateDetail(@Valid @RequestBody AiProductGenerateDTO req) {
        if (req == null || req.getProductName() == null || req.getProductName().trim().isEmpty()) {
            return ResponseEntity.ok(Result.fail("productName 不能为空"));
        }

        try {
            String detail = aiProductService.generateProductDetail(
                    req.getProductName().trim(),
                    req.getSupplier(),
                    req.getCategory()
            );
            return ResponseEntity.ok(Result.ok(detail));
        } catch (Exception e) {
            // 如果发生未预期的异常，返回默认提示，前端可直接填充
            return ResponseEntity.ok(Result.ok("AI生成失败，请手动填写"));
        }
    }


    @GetMapping("/test-detail")
    public Result<String> testDetail() {
        String detail = aiProductService.generateProductDetail(
                "Nike经典运动短袖",
                "Nike官方旗舰店",
                "服装 > 上衣 > 短袖"
        );
        return Result.ok(detail);
    }
}


