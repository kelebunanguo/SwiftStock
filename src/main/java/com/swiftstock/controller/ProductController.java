package com.swiftstock.controller;

import com.swiftstock.entity.Product;
import com.swiftstock.service.CategoryService;
import com.swiftstock.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;

    /**
     * 获取商品列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Product searchParams = new Product();
            searchParams.setName(name);
            searchParams.setCategoryId(categoryId);
            
            List<Product> products = productService.findByCondition(searchParams);
            
            // 简单分页处理
            int start = (page - 1) * size;
            int end = Math.min(start + size, products.size());
            List<Product> pageProducts = products.subList(start, end);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", pageProducts);
            data.put("total", products.size());
            data.put("page", page);
            data.put("size", size);
            
            response.put("success", true);
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取商品列表失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有可用商品（用于订单创建）
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableProducts() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Product> products = productService.findAllAvailable();
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", products);
            data.put("total", products.size());
            
            response.put("success", true);
            response.put("data", data);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取可用商品列表失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProduct(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Product product = productService.findById(id);
            if (product == null) {
                response.put("success", false);
                response.put("message", "商品不存在");
            } else {
                response.put("success", true);
                response.put("data", product);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取商品详情失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 创建商品
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(@RequestBody Product product) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            productService.save(product);
            response.put("success", true);
            response.put("message", "商品创建成功");
            response.put("data", product);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "商品创建失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Product existingProduct = productService.findById(id);
            if (existingProduct == null) {
                response.put("success", false);
                response.put("message", "商品不存在");
            } else {
                product.setId(id);
                productService.save(product);
                response.put("success", true);
                response.put("message", "商品更新成功");
                response.put("data", product);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "商品更新失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            productService.deleteById(id);
            response.put("success", true);
            response.put("message", "商品删除成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "商品删除失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取商品分类列表
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<com.swiftstock.entity.Category> categories = categoryService.findAll();
            response.put("success", true);
            response.put("data", categories);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取分类列表失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

}