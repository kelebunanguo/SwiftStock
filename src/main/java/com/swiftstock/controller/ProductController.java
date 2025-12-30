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
import com.swiftstock.dto.Result;

/**
 * 商品管理接口（Controller）
 *
 * <p>业务对象：商品（product），包含价格、库存、安全库存阈值、库位等信息。
 * <p>设计要点：
 * <ul>
 *   <li>商品列表支持条件查询（名称/分类）与分页</li>
 *   <li>订单创建时通过 {@code /products/available} 只返回“有库存的商品”</li>
 * </ul>
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;

    /**
     * 获取商品列表
     *
     * <p>分页说明：当前为内存分页（subList），适合演示；生产环境建议在 Mapper 层做分页。
     */
    @GetMapping
    public ResponseEntity<Result<Map<String, Object>>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

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

            return ResponseEntity.ok(Result.ok(data));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取商品列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取所有可用商品（用于订单创建）
     *
     * <p>可用的定义：库存 > 0。用于避免下单时选择到“缺货商品”。
     */
    @GetMapping("/available")
    public ResponseEntity<Result<Map<String, Object>>> getAvailableProducts() {
        try {
            List<Product> products = productService.findAllAvailable();

            Map<String, Object> data = new HashMap<>();
            data.put("list", products);
            data.put("total", products.size());

            return ResponseEntity.ok(Result.ok(data));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取可用商品列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<Product>> getProduct(@PathVariable Long id) {
        try {
            Product product = productService.findById(id);
            if (product == null) {
                return ResponseEntity.ok(Result.fail("商品不存在"));
            } else {
                return ResponseEntity.ok(Result.ok(product));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取商品详情失败：" + e.getMessage()));
        }
    }

    /**
     * 创建商品
     *
     * <p>数据库层存在触发器自动生成商品编码 {@code product.code}（见 SQL 脚本）。
     */
    @PostMapping
    public ResponseEntity<Result<Product>> createProduct(@RequestBody Product product) {
        try {
            productService.save(product);
            return ResponseEntity.ok(Result.ok(product, "商品创建成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("商品创建失败：" + e.getMessage()));
        }
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<Product>> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            Product existingProduct = productService.findById(id);
            if (existingProduct == null) {
                return ResponseEntity.ok(Result.fail("商品不存在"));
            } else {
                product.setId(id);
                productService.save(product);
                return ResponseEntity.ok(Result.ok(product, "商品更新成功"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("商品更新失败：" + e.getMessage()));
        }
    }

    /**
     * 删除商品
     *
     * <p>注意：商品如果被订单项引用，数据库外键约束可能会阻止删除（取决于约束配置）。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteById(id);
            return ResponseEntity.ok(Result.ok(null, "商品删除成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("商品删除失败：" + e.getMessage()));
        }
    }

    /**
     * 获取商品分类列表
     *
     * <p>说明：系统同时提供 {@code /categories} 独立分类接口；此处是商品模块下的便捷入口。
     */
    @GetMapping("/categories")
    public ResponseEntity<Result<List<com.swiftstock.entity.Category>>> getCategories() {
        try {
            List<com.swiftstock.entity.Category> categories = categoryService.findAll();
            return ResponseEntity.ok(Result.ok(categories));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取分类列表失败：" + e.getMessage()));
        }
    }

}