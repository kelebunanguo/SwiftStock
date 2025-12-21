package com.swiftstock.mapper;

import com.swiftstock.entity.Supplier;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SupplierMapper {
    Supplier findById(@Param("id") Long id);
    List<Supplier> findAll();
    List<Supplier> findPage(@Param("name") String name, @Param("contactPerson") String contactPerson, @Param("offset") Integer offset, @Param("size") Integer size);
    long count(@Param("name") String name, @Param("contactPerson") String contactPerson);
    int insert(Supplier supplier);
    int update(Supplier supplier);
    int deleteById(@Param("id") Long id);
}


