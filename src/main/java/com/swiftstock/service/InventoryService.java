package com.swiftstock.service;

import com.swiftstock.entity.InventoryRecord;
import java.util.List;

public interface InventoryService {
    List<InventoryRecord> findByProductId(Long productId);
    void addStock(Long productId, Integer quantity, String reason);
    void reduceStock(Long productId, Integer quantity, String reason);
} 