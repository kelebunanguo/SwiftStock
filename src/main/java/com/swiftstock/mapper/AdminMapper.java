package com.swiftstock.mapper;

import com.swiftstock.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {
    
    Admin findById(Long id);
    
    Admin findByUsername(String username);
    
    int update(Admin admin);
} 