package com.swiftstock.controller;

import com.swiftstock.entity.InventoryRecord;
import com.swiftstock.entity.Product;
import com.swiftstock.service.InventoryService;
import com.swiftstock.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * 库存管理接口（Controller）
 *
 * <p>职责：
 * <ul>
 *   <li>库存列表查询（支持条件筛选与分页）</li>
 *   <li>入库/出库操作（落库库存记录）</li>
 *   <li>库存操作类型封装（采购/退货/调拨/损耗等映射到入/出库）</li>
 * </ul>
 *
 * <p>说明：库存的最终落库更新由 {@link com.swiftstock.service.InventoryService} 与
 * {@link com.swiftstock.service.ProductService} 协同完成：
 * <ul>
 *   <li>商品表 {@code product.stock_quantity} 保存“当前库存”</li>
 *   <li>库存记录表 {@code inventory_record} 保存“变动明细”（可追溯）</li>
 * </ul>
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private ProductService productService;
    
    /**
     * 获取库存列表
     *
     * <p>stockStatus 约定：
     * <ul>
     *   <li>{@code out}：缺货（stock_quantity == 0）</li>
     *   <li>{@code low}：低库存（0 < stock_quantity <= min_stock_level）</li>
     *   <li>{@code normal}：正常库存（stock_quantity > min_stock_level）</li>
     * </ul>
     *
     * <p>分页说明：当前为内存分页（subList），适用于毕业设计演示；生产环境建议数据库分页。
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getInventoryList(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Accessing inventory list with params: productName={}, categoryId={}, stockStatus={}", 
                       productName, categoryId, stockStatus);
            
            List<Product> products = productService.findAll();
            
            // 1) 应用搜索过滤：商品名称模糊匹配
            if (productName != null && !productName.trim().isEmpty()) {
                products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(productName.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 2) 分类筛选：按 categoryId 过滤
            if (categoryId != null) {
                products = products.stream()
                    .filter(p -> p.getCategoryId() != null && p.getCategoryId().equals(categoryId))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            if (stockStatus != null && !stockStatus.trim().isEmpty()) {
                logger.info("Applying stock status filter: {}", stockStatus);
                List<Product> beforeFilter = new ArrayList<>(products);
                products = products.stream()
                    .filter(p -> {
                        boolean matches = false;
                        switch (stockStatus.toLowerCase()) {
                            case "out":
                                matches = p.getStockQuantity() == 0;
                                break;
                            case "low":
                                matches = p.getStockQuantity() > 0 && p.getStockQuantity() <= p.getMinStockLevel();
                                break;
                            case "normal":
                                matches = p.getStockQuantity() > p.getMinStockLevel();
                                break;
                            default:
                                matches = true;
                        }
                        logger.debug("Product {}: stock={}, minStock={}, matches={}", 
                                   p.getName(), p.getStockQuantity(), p.getMinStockLevel(), matches);
                        return matches;
                    })
                    .collect(java.util.stream.Collectors.toList());
                logger.info("Stock status filter applied: {} -> {} products", beforeFilter.size(), products.size());
            }
            
            if (products.isEmpty()) {
                logger.warn("No products found matching the search criteria");
            } else {
                logger.info("Found {} products matching the search criteria", products.size());
            }
            
            // 3) 简单分页处理
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
            logger.error("Error while retrieving product list", e);
            response.put("success", false);
            response.put("message", "获取库存列表失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取商品库存记录
     *
     * <p>返回结构：{@code {product: 商品信息, records: 库存变动列表}}。
     */
    @GetMapping("/records/{productId}")
    public ResponseEntity<Map<String, Object>> getInventoryRecords(@PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("Accessing inventory record with product ID: {}", productId);
            
            // 获取商品信息
            Product product = productService.findById(productId);
            if (product == null) {
                logger.error("Product not found with ID: {}", productId);
                response.put("success", false);
                response.put("message", "商品不存在");
                return ResponseEntity.ok(response);
            }
            
            // 获取库存记录
            List<InventoryRecord> records = inventoryService.findByProductId(productId);
            if (records == null) {
                records = Collections.emptyList();
            }
            
            logger.info("Successfully retrieved product and {} inventory records for product ID: {}", 
                       records.size(), productId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("product", product);
            data.put("records", records);
            
            response.put("success", true);
            response.put("data", data);
        } catch (Exception e) {
            logger.error("Error while retrieving inventory records for product ID: " + productId, e);
            response.put("success", false);
            response.put("message", "获取库存记录失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 商品入库
     *
     * <p>业务含义：库存增加 + 写入一条 {@code inventory_record(type=IN)}。
     */
    @PostMapping("/in")
    public ResponseEntity<Map<String, Object>> stockIn(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            String reason = request.get("reason").toString();
            
            logger.info("Processing stock in request - Product ID: {}, Quantity: {}", productId, quantity);
            
            if (quantity <= 0) {
                logger.warn("Invalid quantity for stock in: {}", quantity);
                response.put("success", false);
                response.put("message", "入库数量必须大于0");
                return ResponseEntity.ok(response);
            }
            
            inventoryService.addStock(productId, quantity, reason);
            
            response.put("success", true);
            response.put("message", "入库操作成功");
        } catch (Exception e) {
            logger.error("Error processing stock in request", e);
            response.put("success", false);
            response.put("message", "入库操作失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 商品出库
     *
     * <p>业务含义：库存减少 + 写入一条 {@code inventory_record(type=OUT)}。
     * 实际扣减会在 {@code ProductService.updateStock} 中校验“不能扣成负库存”。
     */
    @PostMapping("/out")
    public ResponseEntity<Map<String, Object>> stockOut(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            String reason = request.get("reason").toString();
            
            logger.info("Processing stock out request - Product ID: {}, Quantity: {}", productId, quantity);
            
            if (quantity <= 0) {
                logger.warn("Invalid quantity for stock out: {}", quantity);
                response.put("success", false);
                response.put("message", "出库数量必须大于0");
                return ResponseEntity.ok(response);
            }
            
            inventoryService.reduceStock(productId, quantity, reason);
            
            response.put("success", true);
            response.put("message", "出库操作成功");
        } catch (Exception e) {
            logger.error("Error processing stock out request", e);
            response.put("success", false);
            response.put("message", "出库操作失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 库存操作（支持多种操作类型）
     *
     * <p>该接口是“业务动作”到“库存增减”的映射层：
     * <ul>
     *   <li>purchase/return/transfer_in => 入库</li>
     *   <li>sales/transfer_out/damage => 出库</li>
     * </ul>
     */
    @PostMapping("/operation")
    public ResponseEntity<Map<String, Object>> stockOperation(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            String reason = request.get("reason").toString();
            String operationType = request.get("operationType").toString();
            
            logger.info("Processing stock operation - Product ID: {}, Quantity: {}, Type: {}", 
                       productId, quantity, operationType);
            
            if (quantity <= 0) {
                logger.warn("Invalid quantity for stock operation: {}", quantity);
                response.put("success", false);
                response.put("message", "操作数量必须大于0");
                return ResponseEntity.ok(response);
            }
            
            // 根据操作类型处理
            switch (operationType) {
                case "purchase":
                case "return":
                case "transfer_in":
                    inventoryService.addStock(productId, quantity, reason);
                    break;
                case "sales":
                case "transfer_out":
                case "damage":
                    inventoryService.reduceStock(productId, quantity, reason);
                    break;
                default:
                    response.put("success", false);
                    response.put("message", "不支持的操作类型");
                    return ResponseEntity.ok(response);
            }
            
            response.put("success", true);
            response.put("message", "库存操作成功");
        } catch (Exception e) {
            logger.error("Error processing stock operation", e);
            response.put("success", false);
            response.put("message", "库存操作失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }


    /**
     * 批量库存操作
     *
     * <p>说明：当前实现为逐个商品处理，遇到单个失败会跳过并继续（返回成功数/总数）。
     * 因此是“部分成功”的批处理策略，适合演示；如需强一致可改为整体事务回滚。
     */
    @PostMapping("/batch-operation")
    public ResponseEntity<Map<String, Object>> batchStockOperation(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Long> productIds = (List<Long>) request.get("productIds");
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            String reason = request.get("reason").toString();
            String operationType = request.get("operationType").toString();
            
            logger.info("Processing batch stock operation - Products: {}, Quantity: {}, Type: {}", 
                       productIds.size(), quantity, operationType);
            
            if (quantity <= 0) {
                logger.warn("Invalid quantity for batch operation: {}", quantity);
                response.put("success", false);
                response.put("message", "操作数量必须大于0");
                return ResponseEntity.ok(response);
            }
            
            int successCount = 0;
            for (Long productId : productIds) {
                try {
                    switch (operationType) {
                        case "purchase":
                        case "return":
                        case "transfer_in":
                            inventoryService.addStock(productId, quantity, reason);
                            break;
                        case "sales":
                        case "transfer_out":
                        case "damage":
                            inventoryService.reduceStock(productId, quantity, reason);
                            break;
                    }
                    successCount++;
                } catch (Exception e) {
                    logger.warn("Failed to process product ID {}: {}", productId, e.getMessage());
                }
            }
            
            response.put("success", true);
            response.put("message", String.format("批量操作完成，成功处理 %d/%d 个商品", successCount, productIds.size()));
        } catch (Exception e) {
            logger.error("Error processing batch stock operation", e);
            response.put("success", false);
            response.put("message", "批量操作失败：" + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }

}