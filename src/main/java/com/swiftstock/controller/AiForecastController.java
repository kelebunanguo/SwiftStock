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

/**
 * AI 补货预测控制器
 *
 * <p>职责：
 * <ul>
 *   <li>提供基于 AI/大模型的补货预测与建议接口，包括推荐补货数量与详细补货建议列表。</li>
 *   <li>控制器负责 HTTP 层接入、参数校验与统一返回格式，具体预测逻辑由 Service 层实现并可能包含缓存与定时刷新策略。</li>
 * </ul>
 *
 * <p>返回格式：统一使用 {@code {success: boolean, message?: String, data?: Object}} 结构，便于前端统一处理。</p>
 *
 * <p>路径：`/api/ai/forecast`。</p>
 */
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
     * 返回详细补货建议列表（按建议补货数量降序，取前10）
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