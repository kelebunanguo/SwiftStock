package com.swiftstock.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftstock.dto.AiReorderRecommendVO;
import com.swiftstock.entity.Product;
import com.swiftstock.mapper.ProductMapper;
import com.swiftstock.service.AiForecastService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AI 补货预测服务实现类（放在 impl 包中）。
 * 实现包含：调用大模型、解析 JSON、缓存结果、定时刷新等具体逻辑。
 */
@Slf4j
@Service
public class AiForecastServiceImpl implements AiForecastService {

    private final ProductMapper productMapper;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 简单的内存缓存，缓存 key = "recommend_list"
    private final ConcurrentHashMap<String, List<AiReorderRecommendVO>> cache = new ConcurrentHashMap<>();
    private volatile Instant lastRefresh = Instant.EPOCH;
    private final long ttlSeconds = 60 * 30; // 30 minutes

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    public AiForecastServiceImpl(ProductMapper productMapper, ChatClient.Builder chatClientBuilder) {
        this.productMapper = productMapper;
        this.chatClient = chatClientBuilder.build();

        // 定时刷新缓存，避免频繁请求大模型
        this.scheduler.scheduleAtFixedRate(this::refreshCacheSilently, ttlSeconds, ttlSeconds, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    @Override
    public int getRecommendCount() {
        List<AiReorderRecommendVO> list = getRecommendList();
        return list.size();
    }

    @Override
    public List<AiReorderRecommendVO> getRecommendList() {
        // 优先使用缓存
        List<AiReorderRecommendVO> cached = cache.get("recommend_list");
        if (cached != null && Instant.now().minusSeconds(ttlSeconds).isBefore(lastRefresh)) {
            return cached;
        }

        List<AiReorderRecommendVO> list = generateRecommendations();
        // 按建议补货量降序排序并取前10
        list.sort(Comparator.comparing(AiReorderRecommendVO::getSuggestReorderQuantity, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        List<AiReorderRecommendVO> top = list.size() > 10 ? new ArrayList<>(list.subList(0, 10)) : list;

        cache.put("recommend_list", top);
        lastRefresh = Instant.now();
        log.info("AI智能补货推荐计算结束");
        return top;
    }

    // 异常吞并的静默刷新，保证调度器稳定运行
    private void refreshCacheSilently() {
        try {
            List<AiReorderRecommendVO> newList = generateRecommendations();
            newList.sort(Comparator.comparing(AiReorderRecommendVO::getSuggestReorderQuantity, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
            List<AiReorderRecommendVO> top = newList.size() > 15 ? new ArrayList<>(newList.subList(0, 15)) : newList;
            cache.put("recommend_list", top);
            lastRefresh = Instant.now();
        } catch (Exception ignored) {
            // 保持静默，防止调度器中断
        }
    }

    // 生成推荐的具体实现：遍历商品、调用大模型、解析结果并构造 VO 列表
    private List<AiReorderRecommendVO> generateRecommendations() {
        List<AiReorderRecommendVO> result = new ArrayList<>();

        List<Product> products = productMapper.selectAll();
        if (products == null || products.isEmpty()) {
            return result;
        }

        for (Product p : products) {
            try {
                // 仅考虑在售商品（status == 1）
                if (p.getStatus() == null || p.getStatus() != 1) {
                    continue;
                }

                String productName = p.getName();
                String productCode = p.getCode();
                Integer currentStock = p.getStockQuantity() == null ? 0 : p.getStockQuantity();
                Integer minStock = p.getMinStockLevel() == null ? 10 : p.getMinStockLevel();

                // 严格按照模板构造 prompt
                String prompt = ""
                        + "你是一名拥有10年经验的电商仓库管理专家。请根据以下商品信息，分析未来7天的销售情况并给出补货建议。\n\n"
                        + "商品名称：" + productName + "\n"
                        + "商品编码：" + productCode + "\n"
                        + "当前库存：" + currentStock + " 件\n"
                        + "最小库存阈值：" + minStock + " 件\n\n"
                        + "背景信息：\n"
                        + "- 本店主要销售运动服装鞋帽类商品\n"
                        + "- 周末销量通常是工作日的1.5-2倍\n"
                        + "- 近期无大型促销活动\n\n"
                        + "请直接输出纯JSON格式（不要任何其他文字）：\n"
                        + "{\n"
                        + "  \"needReorder\": true 或 false,\n"
                        + "  \"forecastSales7Days\": 整数（你预测的未来7天销量，合理范围10-100）, \n"
                        + "  \"suggestReorderQuantity\": 整数（建议补货数量，如果不需要补货填0，取10的倍数向上取整，最小补货单位10件）, \n"
                        + "  \"advice\": \"50-80字自然流畅的中文建议，语气专业，像人类专家写的一样，突出缺货风险、补货紧迫性和理由\"\n"
                        + "}\n";

                String aiResponse = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();

                if (aiResponse == null || aiResponse.trim().isEmpty()) {
                    continue;
                }

                // 解析 JSON
                JsonNode root = objectMapper.readTree(aiResponse);
                if (root == null || !root.has("needReorder")) {
                    continue;
                }

                boolean needReorder = root.get("needReorder").asBoolean(false);
                if (!needReorder) {
                    continue;
                }

                int forecastSales7Days = root.has("forecastSales7Days") ? root.get("forecastSales7Days").asInt(0) : 0;
                int suggestReorderQuantity = root.has("suggestReorderQuantity") ? root.get("suggestReorderQuantity").asInt(0) : 0;
                String advice = root.has("advice") ? root.get("advice").asText("") : "";

                // 将补货数量向上取整为 10 的倍数
                if (suggestReorderQuantity > 0 && suggestReorderQuantity % 10 != 0) {
                    suggestReorderQuantity = ((suggestReorderQuantity + 9) / 10) * 10;
                }

                AiReorderRecommendVO vo = new AiReorderRecommendVO();
                vo.setProductId(p.getId());
                vo.setProductName(productName);
                vo.setProductCode(productCode);
                vo.setCurrentStock(currentStock);
                vo.setMinStock(minStock);
                vo.setForecastSales7Days(forecastSales7Days);
                vo.setSuggestReorderQuantity(suggestReorderQuantity);
                vo.setAdvice(advice);

                result.add(vo);

            } catch (Exception e) {
                // 忽略单个商品的异常，保持整体流程健壮性
            }
        }

        return result;
    }
}


