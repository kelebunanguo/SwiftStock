package com.swiftstock.service;

import com.swiftstock.entity.Admin;

/**
 * 管理员服务接口
 * - 提供对管理员信息的查询接口，通常供认证/授权模块使用。
 */

public interface AdminService {
    Admin findById(Long id);
    Admin findByUsername(String username);
} 