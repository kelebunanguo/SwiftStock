package com.swiftstock.service;

import com.swiftstock.entity.Admin;

/**
 * 管理员服务接口
 * - 提供对管理员信息的查询接口，通常供认证/授权模块使用。
 */

public interface AdminService {
    /**
     * 根据 ID 查询管理员信息
     *
     * @param id 管理员 ID
     * @return 管理员实体或 null
     */
    Admin findById(Long id);

    /**
     * 根据用户名查询管理员（用于认证）
     *
     * @param username 登录用户名
     * @return 管理员实体或 null
     */
    Admin findByUsername(String username);
} 