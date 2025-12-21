package com.swiftstock.mapper;

import com.swiftstock.entity.SupplyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SupplyRecordMapper {
    List<SupplyRecord> findBySupplierId(@Param("supplierId") Long supplierId);
}


