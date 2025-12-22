package com.swiftstock.service.impl;

import com.swiftstock.entity.Product;
import com.swiftstock.service.ProductService;
import com.swiftstock.service.StockAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 库存预警服务实现
 */
@Slf4j
@Service
public class StockAlertServiceImpl implements StockAlertService {
    
    @Autowired
    private ProductService productService;
    
    @Override
    public List<Product> checkAllStockAlerts() {
        log.info("开始检查所有商品的库存预警状态");
        List<Product> allProducts = productService.findAll();
        
        List<Product> lowStockProducts = allProducts.stream()
            .filter(this::isLowStock)
            .collect(Collectors.toList());
        
        if (!lowStockProducts.isEmpty()) {
            log.warn("发现{}个商品库存不足，需要预警", lowStockProducts.size());
            lowStockProducts.forEach(product -> 
                log.warn("商品[{}]当前库存{}件，安全库存阈值{}件", 
                    product.getName(), product.getStockQuantity(), product.getMinStockLevel()));
        } else {
            log.info("所有商品库存状态正常");
        }
        
        return lowStockProducts;
    }
    
    /**
     * 检查指定商品是否触发预警
     *
     * @param productId 商品 ID
     * @return 是否触发预警（低库存或缺货）
     */
    @Override
    public boolean checkStockAlert(Long productId) {
        Product product = productService.findById(productId);
        if (product == null) {
            log.warn("商品ID{}不存在", productId);
            return false;
        }
        
        boolean isAlert = isLowStock(product);
        if (isAlert) {
            log.warn("商品[{}]触发库存预警：当前库存{}件，安全库存阈值{}件", 
                product.getName(), product.getStockQuantity(), product.getMinStockLevel());
        }
        
        return isAlert;
    }
    
    /**
     * 获取低库存（>0 且 <= minStockLevel）的商品数量
     *
     * @return 低库存商品数量
     */
    @Override
    public int getLowStockCount() {
        List<Product> allProducts = productService.findAll();
        return (int) allProducts.stream()
            .filter(p -> p.getStockQuantity() > 0 && p.getStockQuantity() <= p.getMinStockLevel())
            .count();
    }
    
    /**
     * 获取缺货（stock_quantity == 0）的商品数量
     *
     * @return 缺货商品数量
     */
    @Override
    public int getOutOfStockCount() {
        List<Product> allProducts = productService.findAll();
        return (int) allProducts.stream()
            .filter(p -> p.getStockQuantity() == 0)
            .count();
    }
    
    /**
     * 判断商品是否库存不足
     * @param product 商品
     * @return true-库存不足，false-库存充足
     */
    private boolean isLowStock(Product product) {
        if (product == null) {
            return false;
        }
        
        // 库存为0或库存数量小于等于安全库存阈值
        return product.getStockQuantity() <= product.getMinStockLevel();
    }
}
