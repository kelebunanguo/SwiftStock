package com.swiftstock.service.impl;

import com.swiftstock.entity.Category;
import com.swiftstock.mapper.CategoryMapper;
import com.swiftstock.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品分类服务实现类，负责调用 Mapper 完成数据库操作。
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public Category findById(Long id) {
        return categoryMapper.findById(id);
    }

    @Override
    public List<Category> findAll() {
        return categoryMapper.findAll();
    }

    @Override
    public void save(Category category) {
        if (category.getId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.update(category);
        }
    }

    @Override
    public void delete(Long id) {
        categoryMapper.delete(id);
    }
}


