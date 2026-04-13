# AI补货推荐核心代码（论文用）

## 1. 历史销量查询

**OrderItemMapper.java**
```java
Integer sumSalesQuantityByProductId(@Param("productId") Long productId, 
                                   @Param("startDate") String startDate, 
                                   @Param("endDate") String endDate);
```

**OrderItemMapper.xml**
```xml
<select id="sumSalesQuantityByProductId" resultType="java.lang.Integer">
    SELECT COALESCE(SUM(i.quantity), 0)
    FROM order_item i
    INNER JOIN orders o ON i.order_id = o.id
    WHERE i.product_id = #{productId}
      AND o.status IN ('PAID', 'PREPARING', 'SHIPPED', 'DELIVERED', 'COMPLETED')
      AND o.created_time >= #{startDate}
      AND o.created_time <= #{endDate}
</select>
```

---

## 2. 增强版Prompt构建

```java
private String buildEnhancedPrompt(String productName, String productCode, 
                                   int currentStock, int minStock, double price,
                                   int recent30DaysSales, double dailyAvgSales,
                                   String seasonalTrend) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("你是一名拥有10年经验的电商仓库管理专家。请根据以下商品完整数据，");
    prompt.append("分析未来7天的销售情况并给出专业补货建议。\n\n");
    
    // 商品基本信息
    prompt.append("【商品基本信息】\n");
    prompt.append("- 商品名称：").append(productName).append("\n");
    prompt.append("- 当前库存：").append(currentStock).append(" 件\n");
    prompt.append("- 最小安全库存：").append(minStock).append(" 件\n");
    prompt.append("- 商品单价：").append(String.format("%.2f", price)).append(" 元\n\n");
    
    // 历史销售数据
    prompt.append("【历史销售数据】\n");
    prompt.append("- 近30天总销量：").append(recent30DaysSales).append(" 件\n");
    prompt.append("- 近30天日均销量：").append(String.format("%.1f", dailyAvgSales)).append(" 件/天\n\n");
    
    // 季节性趋势
    prompt.append("【季节性趋势分析】\n").append("- ").append(seasonalTrend).append("\n\n");
    
    // 库存风险评估
    prompt.append("【库存风险评估】\n");
    int canSustainDays = (dailyAvgSales > 0) ? (int)(currentStock / dailyAvgSales) : 999;
    if (canSustainDays <= 7) prompt.append("- 当前库存可支撑约 ").append(canSustainDays)
                                   .append(" 天（库存紧张，亟需补货）\n");
    else prompt.append("- 当前库存可支撑约 ").append(canSustainDays).append(" 天\n");
    
    if (currentStock < minStock) 
        prompt.append("- 库存低于安全库存，存在缺货风险\n");
    
    // 输出要求
    prompt.append("\n请直接输出纯JSON格式：\n");
    prompt.append("{ \"needReorder\": true/false, \"forecastSales7Days\": 整数, ");
    prompt.append("\"suggestReorderQuantity\": 整数(10倍数), \"advice\": \"60字建议\" }");
    
    return prompt.toString();
}
```

---

## 3. 异步处理方法

```java
@Async
public CompletableFuture<AiReorderRecommendVO> processProductAsync(Product product) {
    return CompletableFuture.supplyAsync(() -> {
        // 获取历史销量数据
        int[] salesData = getHistoricalSalesData(product.getId());
        int recent30DaysSales = salesData[0];
        int previous30DaysSales = salesData[1];
        double dailyAvgSales = recent30DaysSales / 30.0;
        
        // 计算季节性趋势
        String seasonalTrend = calculateSeasonalTrend(recent30DaysSales, previous30DaysSales);
        
        // 构建增强Prompt并调用模型
        String prompt = buildEnhancedPrompt(product.getName(), product.getCode(),
            product.getStockQuantity(), product.getMinStockLevel(),
            product.getPrice().doubleValue(), recent30DaysSales, dailyAvgSales, seasonalTrend);
        
        String aiResponse = chatClient.prompt().user(prompt).call().content();
        JsonNode root = objectMapper.readTree(aiResponse);
        
        if (root.get("needReorder").asBoolean(false)) {
            int suggest = root.get("suggestReorderQuantity").asInt(0);
            suggest = ((suggest + 9) / 10) * 10; // 向上取整10倍数
            
            AiReorderRecommendVO vo = new AiReorderRecommendVO();
            vo.setProductId(product.getId());
            vo.setProductName(product.getName());
            vo.setCurrentStock(product.getStockQuantity());
            vo.setForecastSales7Days(root.get("forecastSales7Days").asInt());
            vo.setSuggestReorderQuantity(suggest);
            vo.setAdvice(root.get("advice").asText());
            return vo;
        }
        return null;
    });
}
```

---

## 4. 历史销量查询与趋势计算

```java
private int[] getHistoricalSalesData(Long productId) {
    LocalDate today = LocalDate.now();
    String todayStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    
    // 近30天
    Integer recent = orderItemMapper.sumSalesQuantityByProductId(
        productId, today.minusDays(30).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00",
        todayStr + " 23:59:59");
    
    // 前30天（对比用）
    Integer previous = orderItemMapper.sumSalesQuantityByProductId(
        productId, today.minusDays(60).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00",
        today.minusDays(31).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 23:59:59");
    
    return new int[]{recent != null ? recent : 0, previous != null ? previous : 0};
}

private String calculateSeasonalTrend(int recent, int previous) {
    if (previous == 0) return recent > 0 ? "新增热销商品" : "新品上市";
    double rate = ((double)(recent - previous) / previous) * 100;
    if (rate >= 50) return "销量大幅增长"+rate+"%，进入热销期";
    else if (rate >= 20) return "销量稳步上升"+rate+"%，市场需求旺盛";
    else if (rate >= 0) return "销量小幅增长"+rate+"%，市场稳定";
    else return "销量下降"+Math.abs(rate)+"%，需关注风险";
}
```
