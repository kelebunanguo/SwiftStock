package com.swiftstock.mapper;

import com.swiftstock.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
/**
 * 管理员Mapper
 */
@Mapper
public interface AdminMapper {
    
    /**
     * 根据 ID 查询管理员
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

    /**
     * 更新管理员信息
     *
     * @param admin 管理员实体（根据 id 更新）
     * @return 受影响的行数
     */
    int update(Admin admin);
} 