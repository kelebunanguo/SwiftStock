package com.swiftstock.service.impl;

import com.swiftstock.entity.Admin;
import com.swiftstock.mapper.AdminMapper;
import com.swiftstock.service.AdminService;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {
    
    private final AdminMapper adminMapper;
    
    public AdminServiceImpl(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }
    
    @Override
    public Admin findById(Long id) {
        return adminMapper.findById(id);
    }

    @Override
    public Admin findByUsername(String username) {
        return adminMapper.findByUsername(username);
    }
} 