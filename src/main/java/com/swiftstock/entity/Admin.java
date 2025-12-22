package com.swiftstock.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员实体
 *
 * <p>保存系统管理员账号信息，用于认证/鉴权与审计记录（不包含敏感处理逻辑）。</p>
 */

 
@Data
public class Admin {
    private Long id;
    private String username;
    private String password;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
} 