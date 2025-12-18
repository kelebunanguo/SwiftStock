package com.swiftstock.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
/**
 * 认证接口（Controller）
 *
 * <p>说明：当前实现为“毕业设计演示版”登录：
 * <ul>
 *   <li>使用固定账号 {@code admin/admin} 校验</li>
 *   <li>返回 {@code mock-jwt-token} 作为前端存储的 token</li>
 * </ul>
 *
 * <p>扩展建议：接入真正的 Spring Security + JWT（或 Session）并基于数据库 admin 表进行校验与加密验证。
 */
public class AuthController {

    /**
     * 用户登录
     *
     * <p>返回结构示例：
     * <pre>
     * { "success": true, "message": "登录成功", "data": { "token": "...", "username": "...", "role": "ADMIN" } }
     * </pre>
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        // 简单的登录验证（实际项目中应该使用 Spring Security + 密码哈希校验）
        if ("admin".equals(username) && "admin".equals(password)) {
            response.put("success", true);
            response.put("message", "登录成功");
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", "mock-jwt-token");
            data.put("username", username);
            data.put("role", "ADMIN");
            response.put("data", data);
        } else {
            response.put("success", false);
            response.put("message", "用户名或密码错误");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 用户登出
     *
     * <p>当前为无状态 token 演示，登出仅返回成功；前端会删除 localStorage 中的 token。
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "登出成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 获取用户信息
     *
     * <p>演示用途：固定返回管理员信息。后续可从 token 或数据库加载真实用户信息。
     */
    @GetMapping("/userinfo")
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> data = new HashMap<>();
        data.put("username", "admin");
        data.put("name", "系统管理员");
        data.put("role", "ADMIN");
        
        response.put("success", true);
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }
}


