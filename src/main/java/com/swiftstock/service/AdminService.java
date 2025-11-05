package com.swiftstock.service;

import com.swiftstock.entity.Admin;

public interface AdminService {
    Admin findById(Long id);
    Admin findByUsername(String username);
} 