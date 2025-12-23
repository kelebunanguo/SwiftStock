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
 * <p>提供分类的查询接口，用于商品管理中的分类展示。</p>
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
}
