package com.swiftstock.service.impl;

import com.swiftstock.entity.InventoryRecord;
import com.swiftstock.mapper.InventoryRecordMapper;
import com.swiftstock.service.InventoryService;
import com.swiftstock.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Service
@Transactional
/**
 * 库存服务实现（Service）
 *
 * <p>设计思路：
 * <ul>
 *   <li>库存“当前值”保存在商品表 {@code product.stock_quantity}</li>
 *   <li>库存“变动明细”保存在 {@code inventory_record}，用于追溯与审计</li>
 *   <li>先更新商品库存，再写入库存记录；两者在事务内保证一致</li>
 * </ul>
 */
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRecordMapper inventoryRecordMapper;

    @Autowired
    private ProductService productService;

    @Override
    public List<InventoryRecord> findByProductId(Long productId) {
        log.debug("Finding inventory records for product ID: {}", productId);
        try {
            // 查询指定商品的库存变动历史（按 mapper 定义的排序输出）
            List<InventoryRecord> records = inventoryRecordMapper.findByProductId(productId);
            log.debug("Found {} records for product ID: {}", records.size(), productId);
            return records;
        } catch (Exception e) {
            log.error("Error finding inventory records for product ID: {}", productId, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void addStock(Long productId, Integer quantity, String reason) {
        log.debug("Adding stock for product ID: {}, quantity: {}, reason: {}", productId, quantity, reason);
        if (quantity <= 0) {
            log.error("Invalid quantity: {}", quantity);
            throw new IllegalArgumentException("入库数量必须大于0");
        }

        try {
            // 1) 更新商品库存：product.stock_quantity += quantity
            productService.updateStock(productId, quantity);

            // 2) 写入库存记录：inventory_record(type=IN)
            InventoryRecord record = new InventoryRecord();
            record.setProductId(productId);
            record.setQuantity(quantity);
            record.setType("IN");
            record.setReason(reason);
            inventoryRecordMapper.insert(record);
            log.debug("Successfully added stock for product ID: {}", productId);
        } catch (Exception e) {
            log.error("Error adding stock for product ID: {}", productId, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void reduceStock(Long productId, Integer quantity, String reason) {
        log.debug("Reducing stock for product ID: {}, quantity: {}, reason: {}", productId, quantity, reason);
        if (quantity <= 0) {
            log.error("Invalid quantity: {}", quantity);
            throw new IllegalArgumentException("出库数量必须大于0");
        }

        try {
            // 1) 更新商品库存：product.stock_quantity -= quantity
            //    注意：负库存校验在 ProductService.updateStock 中完成（newStock < 0 会抛异常并回滚）
            productService.updateStock(productId, -quantity);

            // 2) 写入库存记录：inventory_record(type=OUT)
            InventoryRecord record = new InventoryRecord();
            record.setProductId(productId);
            record.setQuantity(quantity);
            record.setType("OUT");
            record.setReason(reason);
            inventoryRecordMapper.insert(record);
            log.debug("Successfully reduced stock for product ID: {}", productId);
        } catch (Exception e) {
            log.error("Error reducing stock for product ID: {}", productId, e);
            throw e;
        }
    }

} 