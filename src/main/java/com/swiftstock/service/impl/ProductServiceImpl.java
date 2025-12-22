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

/**
 * 商品服务实现类（ProductService 的实现）
 *
 * <p>职责：
 * <ul>
 *   <li>提供商品的查询、保存、删除等基本操作</li>
 *   <li>包含库存更新的事务保护（避免负库存）</li>
 * </ul>
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 获取全部商品列表
     *
     * @return 商品列表
     */
    @Override
    public List<Product> findAll() {
        return productMapper.selectAll();
    }

    /**
     * 获取所有有库存的商品（库存 > 0）
     *
     * @return 有库存的商品列表
     */
    @Override
    public List<Product> findAllAvailable() {
        return productMapper.selectAll().stream()
                .filter(p -> p.getStockQuantity() > 0)
                .collect(Collectors.toList());
    }

    /**
     * 获取低库存商品列表（stock_quantity <= min_stock_level）
     *
     * @return 低库存商品列表
     */
    @Override
    public List<Product> findLowStockProducts() {
        return productMapper.selectAll().stream()
                .filter(p -> p.getStockQuantity() <= p.getMinStockLevel())
                .collect(Collectors.toList());
    }

    /**
     * 根据 ID 查询商品
     *
     * @param id 商品 ID
     * @return 商品实体或 null
     */
    @Override
    public Product findById(Long id) {
        return productMapper.selectById(id);
    }

    /**
     * 保存或更新商品
     *
     * @param product 商品实体（id 为空则插入，否则更新）
     */
    @Override
    public void save(Product product) {
        if (product.getId() == null) {
            productMapper.insert(product);
        } else {
            productMapper.update(product);
        }
    }

    /**
     * 删除商品
     *
     * @param id 商品 ID
     */
    @Override
    public void deleteById(Long id) {
        productMapper.deleteById(id);
    }

    /**
     * 按条件查询商品
     *
     * @param condition 查询条件封装的商品实体
     * @return 满足条件的商品列表
     */
    @Override
    public List<Product> findByCondition(Product condition) {
        return productMapper.findByCondition(condition);
    }

    /**
     * 根据增量更新商品库存（支持正负数）。方法内部会校验库存不能为负并在事务中执行。
     *
     * @param productId 商品 ID
     * @param quantity  变更数量（正数为入库，负数为出库）
     */
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

    /**
     * 设置商品库存为指定数量（覆盖）。
     *
     * @param productId 商品 ID
     * @param stock     目标库存（必须 >= 0）
     */
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