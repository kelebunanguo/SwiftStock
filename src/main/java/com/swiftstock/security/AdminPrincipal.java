package com.swiftstock.security;

import lombok.Data;

/**
 * 简单的当前登录用户承载对象（如需，后续可实现 UserDetails）
 */
@Data
public class AdminPrincipal {
    private Long id;
    private String username;
    private String name;

    public AdminPrincipal(Long id, String username, String name) {
        this.id = id;
        this.username = username;
        this.name = name;
    }
}


