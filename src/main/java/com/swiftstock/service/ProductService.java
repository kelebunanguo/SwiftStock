package com.swiftstock.service;

import com.swiftstock.entity.Product;
import java.util.List;

/**
 * 商品服务接口
 * - 定义商品相关的查询与管理操作（包括库存变更），由具体实现负责数据持久化与业务校验。
 */

public interface ProductService {
    List<Product> findAll();
    List<Product> findAllAvailable();
    List<Product> findLowStockProducts();
    Product findById(Long id);
    void save(Product product);
    void deleteById(Long id);
    List<Product> findByCondition(Product condition);
    void updateStock(Long productId, Integer quantity);
    void setStock(Long productId, Integer stock);
} 