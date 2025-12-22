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
 *
 * <p>职责：
 * <ul>
 *   <li>为商品编辑或创建场景提供 AI 文案生成功能，返回适合商品详情页的文本描述。</li>
 *   <li>提供开发/测试接口以便验证 AI 输出，Controller 层负责入参校验与统一返回结构。</li>
 * </ul>
 *
 * <p>行为说明：对关键入参（如 productName）做基础校验，发生异常时返回友好默认提示以保证前端容错。</p>
 *
 * <p>路径：`/api/ai/product`。</p>
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


