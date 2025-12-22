package com.swiftstock.service.impl;

import com.swiftstock.entity.Admin;
import com.swiftstock.mapper.AdminMapper;
import com.swiftstock.service.AdminService;
import org.springframework.stereotype.Service;
/**
 * 管理员服务实现类（AdminService 的实现）
 *
 * <p>封装对管理员表的基本查询操作，调用对应的 AdminMapper。</p>
 */
@Service
public class AdminServiceImpl implements AdminService {
    
    private final AdminMapper adminMapper;
    
    public AdminServiceImpl(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }
    
    /**
     * 根据 ID 查询管理员信息
     *
     * @param id 管理员 ID
     * @return 管理员实体或 null
     */
    @Override
    public Admin findById(Long id) {
        return adminMapper.findById(id);
    }
    
    /**
     * 根据用户名查询管理员（用于认证）
     *
     * @param username 登录用户名
     * @return 管理员实体或 null
     */
    @Override
    public Admin findByUsername(String username) {
        return adminMapper.findByUsername(username);
    }
} 