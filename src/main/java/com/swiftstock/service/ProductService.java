package com.swiftstock.service;

import com.swiftstock.entity.Product;
import java.util.List;

/**
 * 商品服务接口
 * - 定义商品相关的查询与管理操作（包括库存变更），由具体实现负责数据持久化与业务校验。
 */

public interface ProductService {
    /**
     * 获取全部商品列表
     *
     * @return 商品列表
     */
    List<Product> findAll();

    /**
     * 获取所有有库存的商品（库存 > 0）
     *
     * @return 有库存的商品列表
     */
    List<Product> findAllAvailable();

    /**
     * 获取低库存商品列表（stock_quantity <= min_stock_level）
     *
     * @return 低库存商品列表
     */
    List<Product> findLowStockProducts();

    /**
     * 根据 ID 查询商品
     *
     * @param id 商品 ID
     * @return 商品实体或 null
     */
    Product findById(Long id);

    /**
     * 保存或更新商品；若 id 为空则插入，否则更新。
     *
     * @param product 商品实体
     */
    void save(Product product);

    /**
     * 根据 ID 删除商品
     *
     * @param id 商品 ID
     */
    void deleteById(Long id);

    /**
     * 按条件查询商品
     *
     * @param condition 查询条件封装的商品实体
     * @return 满足条件的商品列表
     */
    List<Product> findByCondition(Product condition);

    /**
     * 根据增量更新商品库存（支持正负数），方法实现需保证事务与负库存校验。
     *
     * @param productId 商品 ID
     * @param quantity  变更数量（正数为入库，负数为出库）
     */
    void updateStock(Long productId, Integer quantity);

    /**
     * 设置商品库存为指定数量（覆盖）。
     *
     * @param productId 商品 ID
     * @param stock     目标库存（必须 >= 0）
     */
    void setStock(Long productId, Integer stock);
} 