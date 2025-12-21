package com.swiftstock.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftstock.dto.AiReorderRecommendVO;
import com.swiftstock.entity.Product;
import com.swiftstock.mapper.ProductMapper;
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

@Service
public class AiForecastService {

    private final ProductMapper productMapper;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // simple in-memory cache
    private final ConcurrentHashMap<String, List<AiReorderRecommendVO>> cache = new ConcurrentHashMap<>();
    private volatile Instant lastRefresh = Instant.EPOCH;
    private final long ttlSeconds = 60 * 30; // 30 minutes

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    public AiForecastService(ProductMapper productMapper, ChatClient.Builder chatClientBuilder) {
        this.productMapper = productMapper;
        this.chatClient = chatClientBuilder.build();

        // schedule periodic refresh to avoid frequent token usage
        this.scheduler.scheduleAtFixedRate(this::refreshCacheSilently, ttlSeconds, ttlSeconds, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    public int getRecommendCount() {
        List<AiReorderRecommendVO> list = getRecommendList();
        return list.size();
    }

    public List<AiReorderRecommendVO> getRecommendList() {
        // check cache
        List<AiReorderRecommendVO> cached = cache.get("recommend_list");
        if (cached != null && Instant.now().minusSeconds(ttlSeconds).isBefore(lastRefresh)) {
            return cached;
        }

        List<AiReorderRecommendVO> list = generateRecommendations();
        // sort by suggestReorderQuantity desc and keep top 15
        list.sort(Comparator.comparing(AiReorderRecommendVO::getSuggestReorderQuantity, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        List<AiReorderRecommendVO> top = list.size() > 15 ? new ArrayList<>(list.subList(0, 15)) : list;

        cache.put("recommend_list", top);
        lastRefresh = Instant.now();
        return top;
    }

    private void refreshCacheSilently() {
        try {
            List<AiReorderRecommendVO> newList = generateRecommendations();
            newList.sort(Comparator.comparing(AiReorderRecommendVO::getSuggestReorderQuantity, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
            List<AiReorderRecommendVO> top = newList.size() > 15 ? new ArrayList<>(newList.subList(0, 15)) : newList;
            cache.put("recommend_list", top);
            lastRefresh = Instant.now();
        } catch (Exception ignored) {
            // intentionally ignore to keep scheduler running
        }
    }

    private List<AiReorderRecommendVO> generateRecommendations() {
        List<AiReorderRecommendVO> result = new ArrayList<>();

        List<Product> products = productMapper.selectAll();
        if (products == null || products.isEmpty()) {
            return result;
        }

        for (Product p : products) {
            try {
                // only consider on-sale products (status == 1)
                if (p.getStatus() == null || p.getStatus() != 1) {
                    continue;
                }

                String productName = p.getName();
                String productCode = p.getCode();
                Integer currentStock = p.getStockQuantity() == null ? 0 : p.getStockQuantity();
                Integer minStock = p.getMinStockLevel() == null ? 10 : p.getMinStockLevel();

                // prompt must strictly follow supplied template
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

                // parse JSON
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

                // normalize suggestReorderQuantity to nearest upper 10 if >0
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
                // ignore individual product errors to keep overall process robust
            }
        }

        return result;
    }
}


