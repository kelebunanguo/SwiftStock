package com.swiftstock.mapper;

import com.swiftstock.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductMapper {
    List<Product> selectAll();
    
    Product selectById(@Param("id") Long id);
    
    void insert(Product product);
    
    void update(Product product);
    
    void deleteById(@Param("id") Long id);
    
    List<Product> findByCondition(Product condition);
    
    void updateStock(@Param("id") Long id, @Param("quantity") Integer quantity);
    
    void setStock(@Param("id") Long id, @Param("stock") Integer stock);
    
    List<Product> selectByCondition(Product condition);
} 