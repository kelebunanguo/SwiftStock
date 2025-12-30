package com.swiftstock.controller;

import com.swiftstock.dto.Result;
import com.swiftstock.entity.Category;
import com.swiftstock.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

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
    public ResponseEntity<Result<List<Category>>> getCategories() {
        try {
            List<Category> categories = categoryService.findAll();
            return ResponseEntity.ok(Result.ok(categories));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取分类列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<Category>> getCategory(@PathVariable Long id) {
        try {
            Category category = categoryService.findById(id);
            if (category == null) {
                return ResponseEntity.ok(Result.fail("分类不存在"));
            } else {
                return ResponseEntity.ok(Result.ok(category));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取分类详情失败：" + e.getMessage()));
        }
    }
}
