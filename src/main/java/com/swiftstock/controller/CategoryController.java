package com.swiftstock.controller;

import com.swiftstock.entity.Category;
import com.swiftstock.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品分类管理控制器
 *
 * <p>提供分类的增删改查接口，用于商品管理中的分类维护与展示。</p>
 */
@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCategories() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Category> categories = categoryService.findAll();
            response.put("success", true);
            response.put("data", categories);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取分类列表失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCategory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Category category = categoryService.findById(id);
            if (category == null) {
                response.put("success", false);
                response.put("message", "分类不存在");
            } else {
                response.put("success", true);
                response.put("data", category);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取分类详情失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 创建分类
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody Category category) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            categoryService.save(category);
            response.put("success", true);
            response.put("message", "分类创建成功");
            response.put("data", category);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "分类创建失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Category existingCategory = categoryService.findById(id);
            if (existingCategory == null) {
                response.put("success", false);
                response.put("message", "分类不存在");
            } else {
                category.setId(id);
                categoryService.save(category);
                response.put("success", true);
                response.put("message", "分类更新成功");
                response.put("data", category);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "分类更新失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            categoryService.delete(id);
            response.put("success", true);
            response.put("message", "分类删除成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "分类删除失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
