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
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRecordMapper inventoryRecordMapper;

    @Autowired
    private ProductService productService;

    @Override
    public List<InventoryRecord> findByProductId(Long productId) {
        log.debug("Finding inventory records for product ID: {}", productId);
        try {
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
            // 更新商品库存
            productService.updateStock(productId, quantity);

            // 记录库存变动
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
            // 更新商品库存
            productService.updateStock(productId, -quantity);

            // 记录库存变动
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