package com.swiftstock.service;

import com.swiftstock.dto.AiReorderRecommendVO;
import java.util.List;

/**
 * AI 补货预测服务接口。
 * <p>
 * - 将对外暴露的方法抽象为接口，有利于单元测试和替换实现（例如在测试中注入 Mock 实现）。
 * - 原有实现已移至 `com.swiftstock.service.impl.AiForecastServiceImpl`。
 */
public interface AiForecastService {

    /**
     * 返回需要补货的商品数量（AI 预测结果的条目数）。
     *
     * @return 需要补货的商品数量
     */
    int getRecommendCount();

    /**
     * 返回详细补货建议列表（按建议补货数量降序，取前10）。
     *
     * @return 补货建议列表
     */
    List<AiReorderRecommendVO> getRecommendList();
}


