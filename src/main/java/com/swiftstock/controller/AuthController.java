package com.swiftstock.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.swiftstock.mapper.AdminMapper;
import com.swiftstock.entity.Admin;
import com.swiftstock.security.JwtTokenUtil;
import com.swiftstock.dto.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 认证接口（Controller）
 *
 * <p>说明：
 * <ul>
 *   <li>使用数据库中的管理员账号进行认证（Admin 表）</li>
 *   <li>返回经 JWT 签名的访问令牌（请在生产环境中通过配置/环境变量管理密钥）</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    /**
     * 用户登录
     *
     * <p>返回结构示例：
     * <pre>
     * { "success": true, "message": "登录成功", "data": { "token": "...", "username": "...", "role": "ADMIN" } }
     * </pre>
     */
    @PostMapping("/login")
    public ResponseEntity<Result<Map<String, Object>>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (username == null || password == null) {
            return ResponseEntity.ok(Result.fail("缺少用户名或密码"));
        }

        logger.info("Login attempt for username={}", username);
        Admin admin = adminMapper.findByUsername(username);
        if (admin == null) {
            logger.warn("Login failed: user not found username={}", username);
            return ResponseEntity.ok(Result.fail("用户不存在"));
        }

        // 比对 bcrypt 哈希密码，捕获异常并打印调试信息
        boolean matches = false;
        try {
            logger.debug("Stored password hash for username={} : {}", username, admin.getPassword());
            matches = passwordEncoder.matches(password, admin.getPassword());
            logger.debug("Password match result for username={} : {}", username, matches);
        } catch (Exception ex) {
            logger.error("Error while matching password for username={}", username, ex);
            return ResponseEntity.ok(Result.fail("服务器内部错误：密码校验失败"));
        }

        if (!matches) {
            logger.warn("Login failed: invalid credentials for username={}", username);
            return ResponseEntity.ok(Result.fail("用户名或密码错误"));
        }

        // 生成 JWT
        String token = jwtTokenUtil.generateToken(admin);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", admin.getUsername());
        data.put("name", admin.getName());
        data.put("role", "ADMIN");

        return ResponseEntity.ok(Result.ok(data, "登录成功"));
    }

    /**
     * 用户登出
     *
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<Void>> logout() {
        return ResponseEntity.ok(Result.ok(null, "登出成功"));
    }

    /**
     * 获取用户信息
     *
     */
    @GetMapping("/userinfo")
    public ResponseEntity<Result<Map<String, Object>>> getUserInfo() {
        // 从 SecurityContext 获取当前认证用户（由 JwtAuthenticationFilter 设置）
        org.springframework.security.core.Authentication authentication =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> data = new HashMap<>();
        if (authentication != null && authentication.getPrincipal() instanceof Admin) {
            Admin admin = (Admin) authentication.getPrincipal();
            data.put("username", admin.getUsername());
            data.put("name", admin.getName());
            data.put("role", "ADMIN");
            return ResponseEntity.ok(Result.ok(data));
        } else {
            // 若未认证则返回未认证提示（不再依赖内置/演示账号）
            return ResponseEntity.status(401).body(Result.fail("用户未认证"));
        }
    }
}


