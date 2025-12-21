package com.swiftstock.controller;

import com.swiftstock.dto.AiReorderRecommendVO;
import com.swiftstock.dto.Result;
import com.swiftstock.service.AiForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai/forecast")
public class AiForecastController {

    @Autowired
    private AiForecastService aiForecastService;

    /**
     * 返回需要补货的商品数量（大模型判断）
     */
    @GetMapping("/recommend-count")
    public ResponseEntity<Result<Integer>> getRecommendCount() {
        try {
            int count = aiForecastService.getRecommendCount();
            return ResponseEntity.ok(Result.ok(count));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取补货数量失败：" + e.getMessage()));
        }
    }

    /**
     * 返回详细补货建议列表（按建议补货数量降序，取前15）
     */
    @GetMapping("/recommend-list")
    public ResponseEntity<Result<List<AiReorderRecommendVO>>> getRecommendList() {
        try {
            List<AiReorderRecommendVO> list = aiForecastService.getRecommendList();
            return ResponseEntity.ok(Result.ok(list));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取补货建议列表失败：" + e.getMessage()));
        }
    }
}