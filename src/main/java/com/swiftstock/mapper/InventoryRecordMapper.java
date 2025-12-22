package com.swiftstock.mapper;

import com.swiftstock.entity.InventoryRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
/**
 * 库存记录Mapper
 */
@Mapper
public interface InventoryRecordMapper {
    /**
     * 根据商品 ID 查询库存变动记录
     *
     * @param productId 商品 ID
     * @return 库存记录列表（按时间或 Mapper 定义的顺序）
     */
    List<InventoryRecord> findByProductId(@Param("productId") Long productId);

    /**
     * 插入一条库存记录（IN/OUT）
     *
     * @param record 库存记录实体
     */
    void insert(InventoryRecord record);
} 