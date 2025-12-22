package com.swiftstock.service;

import com.swiftstock.entity.InventoryRecord;
import java.util.List;

/**
 * 库存服务接口
 * - 提供库存入库/出库及查询历史的操作抽象，具体实现负责事务与库存校验。
 */

public interface InventoryService {
    List<InventoryRecord> findByProductId(Long productId);
    void addStock(Long productId, Integer quantity, String reason);
    void reduceStock(Long productId, Integer quantity, String reason);
} 