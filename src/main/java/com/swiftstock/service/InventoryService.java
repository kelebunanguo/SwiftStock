package com.swiftstock.service;

import com.swiftstock.entity.InventoryRecord;
import java.util.List;

/**
 * 库存服务接口
 * - 提供库存入库/出库及查询历史的操作抽象，具体实现负责事务与库存校验。
 */

public interface InventoryService {
    /**
     * 查询指定商品的库存变动历史
     *
     * @param productId 商品 ID
     * @return 该商品的库存记录列表
     */
    List<InventoryRecord> findByProductId(Long productId);

    /**
     * 为商品增加库存并写入库存记录（入库）
     *
     * @param productId 商品 ID
     * @param quantity  增加数量（必须 > 0）
     * @param reason    入库原因/备注
     */
    void addStock(Long productId, Integer quantity, String reason);

    /**
     * 为商品减少库存并写入库存记录（出库）
     *
     * @param productId 商品 ID
     * @param quantity  减少数量（必须 > 0）
     * @param reason    出库原因/备注
     */
    void reduceStock(Long productId, Integer quantity, String reason);
} 