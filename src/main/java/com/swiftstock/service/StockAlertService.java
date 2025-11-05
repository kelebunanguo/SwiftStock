package com.swiftstock.service;

import com.swiftstock.entity.Product;
import java.util.List;

/**
 * 库存预警服务接口
 */
public interface StockAlertService {
    
    /**
     * 检查所有商品的库存预警状态
     * @return 库存不足的商品列表
     */
    List<Product> checkAllStockAlerts();
    
    /**
     * 检查指定商品的库存预警状态
     * @param productId 商品ID
     * @return 是否触发预警
     */
    boolean checkStockAlert(Long productId);
    
    /**
     * 获取库存不足的商品数量
     * @return 库存不足的商品数量
     */
    int getLowStockCount();
    
    /**
     * 获取缺货商品数量
     * @return 缺货商品数量
     */
    int getOutOfStockCount();
}
