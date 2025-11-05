package com.swiftstock.service;

import com.swiftstock.entity.Category;
import com.swiftstock.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryMapper categoryMapper;
    
    public Category findById(Long id) {
        return categoryMapper.findById(id);
    }
    
    public List<Category> findAll() {
        return categoryMapper.findAll();
    }
    
    public void save(Category category) {
        if (category.getId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.update(category);
        }
    }
    
    public void delete(Long id) {
        categoryMapper.delete(id);
    }
} 