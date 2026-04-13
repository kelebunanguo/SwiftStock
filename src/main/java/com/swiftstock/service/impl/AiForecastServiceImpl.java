package com.swiftstock.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftstock.dto.AiReorderRecommendVO;
import com.swiftstock.entity.Product;
import com.swiftstock.mapper.OrderItemMapper;
import com.swiftstock.mapper.ProductMapper;
import com.swiftstock.service.AiForecastService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AI 补货预测服务实现类
 * 实现包含：调用大模型、解析 JSON、缓存结果、定时刷新等具体逻辑。
 */
@Slf4j
@Service
public class AiForecastServiceImpl implements AiForecastService {

    private final ProductMapper productMapper;
    private final OrderItemMapper orderItemMapper;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 简单的内存缓存，缓存 key = "recommend_list"
    private final ConcurrentHashMap<String, List<AiReorderRecommendVO>> cache = new ConcurrentHashMap<>();
    private volatile Instant lastRefresh = Instant.EPOCH;
    private final long ttlSeconds = 60 * 30; // 30 分钟

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    public AiForecastServiceImpl(ProductMapper productMapper, 
                                  OrderItemMapper orderItemMapper, 
                                  ChatClient.Builder chatClientBuilder) {
        this.productMapper = productMapper;
        this.orderItemMapper = orderItemMapper;
        this.chatClient = chatClientBuilder.build();

        // 定时刷新缓存，避免频繁请求大模型
        this.scheduler.scheduleAtFixedRate(this::refreshCacheSilently, ttlSeconds, ttlSeconds, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    /**
     * 获取需要补货的商品数量（AI 推荐结果条目数）。
     *
     * @return 需要补货的商品数量
     */
    @Override
    public int getRecommendCount() {
        List<AiReorderRecommendVO> list = getRecommendList();
        return list.size();
    }

    /**
     * 获取补货建议列表
     *
     * <p>优先使用缓存，若缓存失效则重新调用模型生成推荐并缓存结果，最终按建议补货量降序并取前 10 条返回。</p>
     *
     * @return 推荐的补货建议 VO 列表（可能为空）
     */
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
            List<AiReorderRecommendVO> top = newList.size() > 10 ? new ArrayList<>(newList.subList(0, 10)) : newList;
            cache.put("recommend_list", top);
            lastRefresh = Instant.now();
        } catch (Exception ignored) {
            // 保持静默，防止调度器中断
        }
    }

    /**
     * 获取商品历史销量数据
     *
     * @param productId 商品ID
     * @return 包含近30天销量、近60天销量、趋势的数组 [recent30Days, previous30Days]
     */
    private int[] getHistoricalSalesData(Long productId) {
        try {
            LocalDate today = LocalDate.now();
            String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String startRecent = today.minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String startPrevious = today.minusDays(60).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endPrevious = today.minusDays(31).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // 查询近30天销量
            Integer recent30Days = orderItemMapper.sumSalesQuantityByProductId(
                productId, 
                startRecent + " 00:00:00", 
                todayStr + " 23:59:59"
            );

            // 查询前30天销量（用于趋势对比）
            Integer previous30Days = orderItemMapper.sumSalesQuantityByProductId(
                productId, 
                startPrevious + " 00:00:00", 
                endPrevious + " 23:59:59"
            );

            return new int[]{
                recent30Days != null ? recent30Days : 0,
                previous30Days != null ? previous30Days : 0
            };
        } catch (Exception e) {
            log.warn("获取商品 {} 历史销量失败: {}", productId, e.getMessage());
            return new int[]{0, 0};
        }
    }

    /**
     * 计算季节性趋势描述
     *
     * @param recentSales 近30天销量
     * @param previousSales 前30天销量
     * @return 趋势描述字符串
     */
    private String calculateSeasonalTrend(int recentSales, int previousSales) {
        if (previousSales == 0) {
            if (recentSales > 0) {
                return "新增热销商品，近期销量上升";
            }
            return "新品上市，暂无历史对比数据";
        }

        double changeRate = ((double) (recentSales - previousSales) / previousSales) * 100;
        
        if (changeRate >= 50) {
            return "销量大幅增长" + String.format("%.1f", changeRate) + "%，进入热销期";
        } else if (changeRate >= 20) {
            return "销量稳步上升，增长" + String.format("%.1f", changeRate) + "%，市场需求旺盛";
        } else if (changeRate >= 0) {
            return "销量小幅增长" + String.format("%.1f", changeRate) + "%，市场稳定";
        } else if (changeRate >= -20) {
            return "销量小幅下降" + String.format("%.1f", Math.abs(changeRate)) + "%，市场正常波动";
        } else {
            return "销量明显下降" + String.format("%.1f", Math.abs(changeRate)) + "%，需关注库存风险";
        }
    }

    /**
     * 异步处理单个商品的AI补货建议
     *
     * @param product 商品对象
     * @return 补货建议VO的CompletableFuture，可能为null（表示不需要补货或处理失败）
     */
    @Async
    public CompletableFuture<AiReorderRecommendVO> processProductAsync(Product product) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 仅考虑在售商品（status == 1）
                if (product.getStatus() == null || product.getStatus() != 1) {
                    return null;
                }

                String productName = product.getName();
                String productCode = product.getCode();
                Integer currentStock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
                Integer minStock = product.getMinStockLevel() == null ? 10 : product.getMinStockLevel();
                Double price = product.getPrice() != null ? product.getPrice().doubleValue() : 0.0;

                // 获取历史销量数据
                int[] salesData = getHistoricalSalesData(product.getId());
                int recent30DaysSales = salesData[0];
                int previous30DaysSales = salesData[1];
                
                // 计算日均销量
                double dailyAvgSales = recent30DaysSales / 30.0;
                
                // 计算季节性趋势
                String seasonalTrend = calculateSeasonalTrend(recent30DaysSales, previous30DaysSales);

                // 构建增强版 Prompt，包含完整信息
                String prompt = buildEnhancedPrompt(productName, productCode, currentStock, 
                    minStock, price, recent30DaysSales, dailyAvgSales, seasonalTrend);

                String aiResponse = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();

                if (aiResponse == null || aiResponse.trim().isEmpty()) {
                    return null;
                }

                // 解析 JSON
                JsonNode root = objectMapper.readTree(aiResponse);
                if (root == null || !root.has("needReorder")) {
                    return null;
                }

                boolean needReorder = root.get("needReorder").asBoolean(false);
                if (!needReorder) {
                    return null;
                }

                int forecastSales7Days = root.has("forecastSales7Days") ? root.get("forecastSales7Days").asInt(0) : 0;
                int suggestReorderQuantity = root.has("suggestReorderQuantity") ? root.get("suggestReorderQuantity").asInt(0) : 0;
                String advice = root.has("advice") ? root.get("advice").asText("") : "";

                // 将补货数量向上取整为 10 的倍数
                if (suggestReorderQuantity > 0 && suggestReorderQuantity % 10 != 0) {
                    suggestReorderQuantity = ((suggestReorderQuantity + 9) / 10) * 10;
                }

                AiReorderRecommendVO vo = new AiReorderRecommendVO();
                vo.setProductId(product.getId());
                vo.setProductName(productName);
                vo.setProductCode(productCode);
                vo.setCurrentStock(currentStock);
                vo.setMinStock(minStock);
                vo.setForecastSales7Days(forecastSales7Days);
                vo.setSuggestReorderQuantity(suggestReorderQuantity);
                vo.setAdvice(advice);

                return vo;

            } catch (Exception e) {
                // 忽略单个商品的异常，返回null
                log.warn("处理商品 {} 的AI补货建议时发生异常: {}", product.getName(), e.getMessage());
                return null;
            }
        });
    }

    /**
     * 构建增强版 Prompt，包含完整的历史销量和趋势信息
     */
    private String buildEnhancedPrompt(String productName, String productCode, 
                                       int currentStock, int minStock, double price,
                                       int recent30DaysSales, double dailyAvgSales,
                                       String seasonalTrend) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一名拥有10年经验的电商仓库管理专家。请根据以下商品完整数据，分析未来7天的销售情况并给出专业补货建议。\n\n");
        prompt.append("【商品基本信息】\n");
        prompt.append("- 商品名称：").append(productName).append("\n");
        prompt.append("- 商品编码：").append(productCode).append("\n");
        prompt.append("- 当前库存：").append(currentStock).append(" 件\n");
        prompt.append("- 最小安全库存：").append(minStock).append(" 件\n");
        prompt.append("- 商品单价：").append(String.format("%.2f", price)).append(" 元\n\n");
        
        prompt.append("【历史销售数据】\n");
        prompt.append("- 近30天总销量：").append(recent30DaysSales).append(" 件\n");
        prompt.append("- 近30天日均销量：").append(String.format("%.1f", dailyAvgSales)).append(" 件/天\n\n");
        
        prompt.append("【季节性趋势分析】\n");
        prompt.append("- ").append(seasonalTrend).append("\n\n");
        
        prompt.append("【库存风险评估】\n");
        // 计算库存可支撑天数
        int canSustainDays = (dailyAvgSales > 0) ? (int)(currentStock / dailyAvgSales) : 999;
        if (canSustainDays > 30) {
            prompt.append("- 当前库存可支撑约 ").append(canSustainDays).append(" 天（库存充足）\n");
        } else if (canSustainDays > 7) {
            prompt.append("- 当前库存可支撑约 ").append(canSustainDays).append(" 天（库存偏紧）\n");
        } else if (canSustainDays > 0) {
            prompt.append("- 当前库存可支撑约 ").append(canSustainDays).append(" 天（库存紧张，亟需补货）\n");
        } else {
            prompt.append("- 当前库存已无法满足日均需求（缺货风险极高）\n");
        }
        
        // 库存与安全库存对比
        if (currentStock < minStock) {
            prompt.append("- 库存低于安全库存 ").append(minStock - currentStock).append(" 件，存在缺货风险\n");
        } else {
            prompt.append("- 库存高于安全库存，暂无缺货风险\n");
        }
        
        prompt.append("\n【背景信息】\n");
        prompt.append("- 本店主要销售运动服装鞋帽类商品\n");
        prompt.append("- 周末销量通常是工作日的1.5-2倍\n");
        prompt.append("- 近期无大型促销活动\n\n");
        
        prompt.append("【输出要求】\n");
        prompt.append("请直接输出纯JSON格式（不要任何其他文字）：\n");
        prompt.append("{\n");
        prompt.append("  \"needReorder\": true 或 false（根据综合分析判断是否需要补货）,\n");
        prompt.append("  \"forecastSales7Days\": 整数（你预测的未来7天销量，应基于历史日均销量和趋势调整，合理范围5-200）,\n");
        prompt.append("  \"suggestReorderQuantity\": 整数（建议补货数量，如果不需要补货填0，必须是10的倍数向上取整，最小补货单位10件）,\n");
        prompt.append("  \"advice\": \"60-100字自然流畅的中文建议，语气专业，像人类专家写的一样，突出缺货风险、补货紧迫性、销量趋势和补货理由\"\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    /**
     * 生成推荐列表的具体实现：并行调用AI模型处理所有商品，显著提升性能
     *
     * <p>使用异步并行调用，每个商品的AI请求同时发送，大幅缩短总等待时间。</p>
     *
     * @return 推荐的 AiReorderRecommendVO 列表（可能为空）
     */
    private List<AiReorderRecommendVO> generateRecommendations() {
        List<AiReorderRecommendVO> result = new ArrayList<>();

        List<Product> products = productMapper.selectAll();
        if (products == null || products.isEmpty()) {
            return result;
        }
        long startTime = System.currentTimeMillis();
        log.info("开始并行处理，时间: {}", LocalDateTime.now());
        log.info("开始并行处理 {} 个商品的AI补货建议", products.size());

        // 创建所有商品的异步调用
        List<CompletableFuture<AiReorderRecommendVO>> futures = new ArrayList<>();
        for (Product product : products) {
            futures.add(processProductAsync(product));
        }

        // 等待所有异步调用完成
        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );
            allFutures.get(); // 阻塞等待所有任务完成

            // 收集所有成功的结果
            for (CompletableFuture<AiReorderRecommendVO> future : futures) {
                try {
                    AiReorderRecommendVO vo = future.get();
                    if (vo != null) {
                        result.add(vo);
                    }
                } catch (Exception e) {
                    // 忽略单个异步任务的异常
                    log.warn("收集AI补货建议结果时发生异常: {}", e.getMessage());
                }
            }
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            double elapsedTimes = elapsedTime / 1000.0; // 转换为秒
            log.info("方法执行完成，耗时: {} ms", elapsedTimes);
            log.info("AI补货建议并行处理完成，共获得 {} 个有效建议", result.size());
            

        } catch (Exception e) {
            log.error("并行处理AI补货建议时发生异常: {}", e.getMessage());
            // 并行调用失败时返回空列表，避免阻塞用户请求
            log.warn("AI服务暂时不可用，返回空结果");
            return new ArrayList<>();
        }

        return result;
    }

}
