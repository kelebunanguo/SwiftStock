package com.swiftstock.mapper;

import com.swiftstock.entity.InventoryRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InventoryRecordMapper {
    List<InventoryRecord> findByProductId(@Param("productId") Long productId);
    void insert(InventoryRecord record);
} 