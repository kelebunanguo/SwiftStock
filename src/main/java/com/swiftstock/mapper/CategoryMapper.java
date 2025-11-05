package com.swiftstock.mapper;

import com.swiftstock.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper {
    Category findById(Long id);
    List<Category> findAll();
    List<Category> findAllTree();
    List<Category> findByParentId(Long parentId);
    List<Category> findRootCategories();
    List<Category> findLeafCategories();
    int insert(Category category);
    int update(Category category);
    int delete(Long id);
    int hasChildren(Long id);
    int hasProducts(Long id);
} 