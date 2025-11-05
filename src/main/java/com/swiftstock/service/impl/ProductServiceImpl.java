package com.swiftstock.service.impl;

import com.swiftstock.entity.Product;
import com.swiftstock.mapper.ProductMapper;
import com.swiftstock.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> findAll() {
        return productMapper.selectAll();
    }

    @Override
    public List<Product> findAllAvailable() {
        return productMapper.selectAll().stream()
                .filter(p -> p.getStockQuantity() > 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findLowStockProducts() {
        return productMapper.selectAll().stream()
                .filter(p -> p.getStockQuantity() <= p.getMinStockLevel())
                .collect(Collectors.toList());
    }

    @Override
    public Product findById(Long id) {
        return productMapper.selectById(id);
    }

    @Override
    public void save(Product product) {
        if (product.getId() == null) {
            productMapper.insert(product);
        } else {
            productMapper.update(product);
        }
    }

    @Override
    public void deleteById(Long id) {
        productMapper.deleteById(id);
    }

    @Override
    public List<Product> findByCondition(Product condition) {
        return productMapper.findByCondition(condition);
    }

    @Override
    @Transactional
    public void updateStock(Long productId, Integer quantity) {
        Product product = findById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在：" + productId);
        }
        
        int newStock = product.getStockQuantity() + quantity;
        if (newStock < 0) {
            throw new RuntimeException("库存不足：" + product.getName());
        }
        
        productMapper.updateStock(productId, quantity);
    }

    @Override
    @Transactional
    public void setStock(Long productId, Integer stock) {
        Product product = findById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在：" + productId);
        }
        
        if (stock < 0) {
            throw new RuntimeException("库存数量不能小于0");
        }
        
        productMapper.setStock(productId, stock);
    }
} 