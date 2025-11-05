package com.swiftstock.service;

import com.swiftstock.entity.Product;
import java.util.List;

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