package com.swiftstock.mapper;

import com.swiftstock.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 商品Mapper
 */
@Mapper
public interface ProductMapper {
    /**
     * 查询所有商品
     *
     * @return 商品列表
     */
    List<Product> selectAll();
    
    /**
     * 根据 ID 查询商品
     *
     * @param id 商品 ID
     * @return 商品实体或 null
     */
    Product selectById(@Param("id") Long id);
    
    /**
     * 插入商品（使用数据库自增主键）
     *
     * @param product 商品实体
     */
    void insert(Product product);
    
    /**
     * 更新商品
     *
     * @param product 商品实体（根据 id 更新）
     */
    void update(Product product);
    
    /**
     * 根据 ID 删除商品
     *
     * @param id 商品 ID
     */
    void deleteById(@Param("id") Long id);
    
    /**
     * 按条件查询商品
     *
     * @param condition 查询条件封装的商品实体
     * @return 商品列表
     */
    List<Product> findByCondition(Product condition);
    
    /**
     * 根据增量更新库存（加/减）
     *
     * @param id       商品 ID
     * @param quantity 变更数量
     */
    void updateStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    /**
     * 覆盖设置库存数量
     *
     * @param id    商品 ID
     * @param stock 目标库存
     */
    void setStock(@Param("id") Long id, @Param("stock") Integer stock);
    
    /**
     * 根据复杂条件查询商品（与 findByCondition 类似，供不同 Mapper XML 使用）
     *
     * @param condition 查询条件
     * @return 商品列表
     */
    List<Product> selectByCondition(Product condition);
} 