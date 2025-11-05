package com.swiftstock.service;

import java.util.List;
import java.util.Map;

public interface SalesService {
    /**
     * 获取销售趋势数据
     * @param period 时间段 (7d, 30d, 90d)
     * @return 销售趋势数据列表
     */
    List<Map<String, Object>> getSalesTrend(String period);
    
    /**
     * 获取销售统计概览
     * @return 销售概览数据
     */
    Map<String, Object> getSalesOverview();
}
