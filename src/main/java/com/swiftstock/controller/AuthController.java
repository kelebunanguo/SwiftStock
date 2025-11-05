package com.swiftstock.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        // 简单的登录验证（实际项目中应该使用 Spring Security）
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


