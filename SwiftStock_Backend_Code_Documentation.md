# SwiftStock 电商仓库管理系统 - 后端代码文档

## 概述

本文档按功能模块分类整理 SwiftStock 电商仓库管理系统的后端代码。项目采用 Spring Boot + MyBatis + MySQL 技术栈，集成 Spring Security + JWT 实现权限控制，并引入 Spring AI 集成大语言模型实现智能化功能。

---

## 目录

1. [权限控制模块](#一权限控制模块)
2. [商品管理模块](#二商品管理模块)
3. [订单管理模块](#三订单管理模块)
4. [供应商管理模块](#四供应商管理模块)
5. [库存管理模块](#五库存管理模块)
6. [数据统计模块](#六数据统计模块)
7. [人工智能辅助模块](#七人工智能辅助模块)
8. [公共组件](#八公共组件)

---

## 一、权限控制模块

### 1.1 模块概述

权限控制模块负责系统的身份认证与访问控制，采用 JWT (JSON Web Token) 无状态认证机制，支持管理员登录、登出、Token 验证等功能。

### 1.2 核心类清单

| 类名 | 类型 | 职责 |
|------|------|------|
| `AuthController` | Controller | 认证接口（登录/登出/用户信息） |
| `SecurityConfig` | Config | Spring Security 配置 |
| `JwtAuthenticationFilter` | Security | JWT 请求过滤与认证解析 |
| `JwtTokenUtil` | Security | JWT Token 生成与验证 |
| `AdminPrincipal` | Security | 登录用户承载对象 |
| `AdminMapper` | Mapper | 管理员数据访问 |
| `Admin` | Entity | 管理员实体 |

### 1.3 认证控制器

**文件路径**: `controller/AuthController.java`

```1:121:src/main/java/com/swiftstock/controller/AuthController.java
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

        // 比对 bcrypt 哈希密码
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
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<Void>> logout() {
        return ResponseEntity.ok(Result.ok(null, "登出成功"));
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/userinfo")
    public ResponseEntity<Result<Map<String, Object>>> getUserInfo() {
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
            return ResponseEntity.status(401).body(Result.fail("用户未认证"));
        }
    }
}
```

### 1.4 Security 配置

**文件路径**: `config/SecurityConfig.java`

```1:60:src/main/java/com/swiftstock/config/SecurityConfig.java
package com.swiftstock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.swiftstock.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Spring Security 配置
 *
 * <p>配置要点：
 * <ul>
 *   <li>配置 HTTP 安全过滤链，定义哪些 URL 需要认证，哪些不需要。</li>
 *   <li>禁用 CSRF 保护（使用 JWT 时通常不需要 CSRF）。</li>
 *   <li>在用户名/密码认证过滤器之前插入 JWT 认证过滤器，确保 JWT 认证优先。</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // 静态资源与 Swagger 接口文档允许匿名访问
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/swagger-ui.html", "/swagger-ui/**", "/doc.html", "/doc.html/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                // 认证相关接口允许匿名访问（登录/登出等）
                .requestMatchers("/auth/**").permitAll()
                // 暴露给前端的公共 API（如无需鉴权的接口）
                .requestMatchers("/api/**").permitAll()
                // 其它未明确允许的请求需要认证
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable());

        // 在用户名/密码认证过滤器之前插入 JWT 认证过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 1.5 JWT 工具类

**文件路径**: `security/JwtTokenUtil.java`

```1:66:src/main/java/com/swiftstock/security/JwtTokenUtil.java
package com.swiftstock.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.swiftstock.entity.Admin;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;
import java.util.Date;

/**
 * JWT 工具类（基于 HMAC SHA256）
 *
 * <p>职责：
 * <ul>
 *   <li>根据 Admin 信息生成 JWT token（包含用户名与 id）</li>
 *   <li>验证并解析 JWT，提取用户名等信息</li>
 * </ul>
 *
 * <p>注意：密钥与过期时间从配置中读取，生产环境请通过环境变量或配置中心管理。</p>
 */
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret:SwiftStockSecretKeyChangeMe}")
    private String secret;

    @Value("${jwt.expiration-millis:86400000}")
    private long expirationMillis;

    private Algorithm algorithm;
    private JWTVerifier verifier;

    @PostConstruct
    public void init() {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).build();
    }

    public String generateToken(Admin admin) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMillis);
        return JWT.create()
                .withIssuer("swiftstock")
                .withSubject(admin.getUsername())
                .withClaim("id", admin.getId())
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .sign(algorithm);
    }

    public DecodedJWT verifyToken(String token) {
        return verifier.verify(token);
    }

    public String getUsernameFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getSubject();
    }
}
```

### 1.6 JWT 认证过滤器

**文件路径**: `security/JwtAuthenticationFilter.java`

```1:65:src/main/java/com/swiftstock/security/JwtAuthenticationFilter.java
package com.swiftstock.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.swiftstock.entity.Admin;
import com.swiftstock.mapper.AdminMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * 从 Authorization: Bearer <token> 中解析 JWT，并把认证信息设置到 SecurityContext。
 *
 * <p>实现说明：
 * <ul>
 *   <li>从请求头读取 Authorization 并解析 token</li>
 *   <li>验证 token 并从中提取用户名</li>
 *   <li>根据用户名加载管理员信息并设置到 Spring Security 的上下文中</li>
 * </ul>
 */
@org.springframework.stereotype.Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // 解析 Authorization 头并校验 JWT，若合法则设置认证信息到 SecurityContext
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String username = jwtTokenUtil.getUsernameFromToken(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Admin admin = adminMapper.findByUsername(username);
                    if (admin != null) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(admin, null, Collections.emptyList());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (JWTVerificationException ex) {
                // token 无效，忽略并继续（请求最终会被拒绝）
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

### 1.7 管理员实体

**文件路径**: `entity/Admin.java`

```1:23:src/main/java/com/swiftstock/entity/Admin.java
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
```

---

## 二、商品管理模块

### 2.1 模块概述

商品管理模块负责商品的 CRUD 操作、分类管理、商品列表查询与筛选等功能。商品是电商系统的核心业务实体，与订单、库存、供应商等模块紧密关联。

### 2.2 核心类清单

| 类名 | 类型 | 职责 |
|------|------|------|
| `ProductController` | Controller | 商品管理 REST API |
| `CategoryController` | Controller | 分类管理 REST API |
| `ProductService` | Service | 商品业务接口 |
| `ProductServiceImpl` | Service | 商品业务实现 |
| `CategoryService` | Service | 分类业务接口 |
| `CategoryServiceImpl` | Service | 分类业务实现 |
| `ProductMapper` | Mapper | 商品数据访问 |
| `CategoryMapper` | Mapper | 分类数据访问 |
| `Product` | Entity | 商品实体 |
| `Category` | Entity | 分类实体 |

### 2.3 商品控制器

**文件路径**: `controller/ProductController.java`

```1:171:src/main/java/com/swiftstock/controller/ProductController.java
package com.swiftstock.controller;

import com.swiftstock.entity.Product;
import com.swiftstock.service.CategoryService;
import com.swiftstock.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.swiftstock.dto.Result;

/**
 * 商品管理接口（Controller）
 *
 * <p>业务对象：商品（product），包含价格、库存、安全库存阈值、库位等信息。
 * <p>设计要点：
 * <ul>
 *   <li>商品列表支持条件查询（名称/分类）与分页</li>
 *   <li>订单创建时通过 {@code /products/available} 只返回"有库存的商品"</li>
 * </ul>
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;

    /**
     * 获取商品列表
     *
     * <p>分页说明：当前为内存分页（subList），适合演示；生产环境建议在 Mapper 层做分页。
     */
    @GetMapping
    public ResponseEntity<Result<Map<String, Object>>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Product searchParams = new Product();
            searchParams.setName(name);
            searchParams.setCategoryId(categoryId);

            List<Product> products = productService.findByCondition(searchParams);

            // 简单分页处理
            int start = (page - 1) * size;
            int end = Math.min(start + size, products.size());
            List<Product> pageProducts = products.subList(start, end);

            Map<String, Object> data = new HashMap<>();
            data.put("list", pageProducts);
            data.put("total", products.size());
            data.put("page", page);
            data.put("size", size);

            return ResponseEntity.ok(Result.ok(data));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取商品列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取所有可用商品（用于订单创建）
     *
     * <p>可用的定义：库存 > 0。用于避免下单时选择到"缺货商品"。
     */
    @GetMapping("/available")
    public ResponseEntity<Result<Map<String, Object>>> getAvailableProducts() {
        try {
            List<Product> products = productService.findAllAvailable();

            Map<String, Object> data = new HashMap<>();
            data.put("list", products);
            data.put("total", products.size());

            return ResponseEntity.ok(Result.ok(data));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取可用商品列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<Product>> getProduct(@PathVariable Long id) {
        try {
            Product product = productService.findById(id);
            if (product == null) {
                return ResponseEntity.ok(Result.fail("商品不存在"));
            } else {
                return ResponseEntity.ok(Result.ok(product));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取商品详情失败：" + e.getMessage()));
        }
    }

    /**
     * 创建商品
     *
     * <p>数据库层存在触发器自动生成商品编码 {@code product.code}（见 SQL 脚本）。
     */
    @PostMapping
    public ResponseEntity<Result<Product>> createProduct(@RequestBody Product product) {
        try {
            productService.save(product);
            return ResponseEntity.ok(Result.ok(product, "商品创建成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("商品创建失败：" + e.getMessage()));
        }
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<Product>> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            Product existingProduct = productService.findById(id);
            if (existingProduct == null) {
                return ResponseEntity.ok(Result.fail("商品不存在"));
            } else {
                product.setId(id);
                productService.save(product);
                return ResponseEntity.ok(Result.ok(product, "商品更新成功"));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("商品更新失败：" + e.getMessage()));
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteById(id);
            return ResponseEntity.ok(Result.ok(null, "商品删除成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("商品删除失败：" + e.getMessage()));
        }
    }

    /**
     * 获取商品分类列表
     */
    @GetMapping("/categories")
    public ResponseEntity<Result<List<com.swiftstock.entity.Category>>> getCategories() {
        try {
            List<com.swiftstock.entity.Category> categories = categoryService.findAll();
            return ResponseEntity.ok(Result.ok(categories));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取分类列表失败：" + e.getMessage()));
        }
    }
}
```

### 2.4 商品服务实现

**文件路径**: `service/impl/ProductServiceImpl.java`

```1:152:src/main/java/com/swiftstock/service/impl/ProductServiceImpl.java
package com.swiftstock.service.impl;

import com.swiftstock.entity.Product;
import com.swiftstock.mapper.ProductMapper;
import com.swiftstock.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现类（ProductService 的实现）
 *
 * <p>职责：
 * <ul>
 *   <li>提供商品的查询、保存、删除等基本操作</li>
 *   <li>包含库存更新的事务保护（避免负库存）</li>
 * </ul>
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 获取全部商品列表
     */
    @Override
    public List<Product> findAll() {
        return productMapper.selectAll();
    }

    /**
     * 获取所有有库存的商品（库存 > 0）
     */
    @Override
    public List<Product> findAllAvailable() {
        return productMapper.selectAll().stream()
                .filter(p -> p.getStockQuantity() > 0)
                .collect(Collectors.toList());
    }

    /**
     * 获取低库存商品列表（stock_quantity <= min_stock_level）
     */
    @Override
    public List<Product> findLowStockProducts() {
        return productMapper.selectAll().stream()
                .filter(p -> p.getStockQuantity() <= p.getMinStockLevel())
                .collect(Collectors.toList());
    }

    /**
     * 根据 ID 查询商品
     */
    @Override
    public Product findById(Long id) {
        return productMapper.selectById(id);
    }

    /**
     * 保存或更新商品
     */
    @Override
    public void save(Product product) {
        if (product.getId() == null) {
            productMapper.insert(product);
        } else {
            productMapper.update(product);
        }
    }

    /**
     * 删除商品
     */
    @Override
    public void deleteById(Long id) {
        productMapper.deleteById(id);
    }

    /**
     * 按条件查询商品
     */
    @Override
    public List<Product> findByCondition(Product condition) {
        return productMapper.findByCondition(condition);
    }

    /**
     * 根据增量更新商品库存（支持正负数）
     */
    @Override
    @Transactional
    public void updateStock(Long productId, Integer quantity) {
        Product product = findById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在：" + productId);
        }
        
        int newStock = product.getStockQuantity() + quantity;
        if (newStock < 0) {
            throw new RuntimeException("库存不足：" + product.getName());
        }
        
        productMapper.updateStock(productId, quantity);
    }

    /**
     * 设置商品库存为指定数量（覆盖）
     */
    @Override
    @Transactional
    public void setStock(Long productId, Integer stock) {
        Product product = findById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在：" + productId);
        }
        
        if (stock < 0) {
            throw new RuntimeException("库存数量不能小于0");
        }
        
        productMapper.setStock(productId, stock);
    }
}
```

### 2.5 商品实体

**文件路径**: `entity/Product.java`

```1:27:src/main/java/com/swiftstock/entity/Product.java
package com.swiftstock.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
public class Product {
    private Long id;
    private String name;
    private String code;
    private Long categoryId;
    private Category category;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer minStockLevel;
    private String supplier;
    private Integer status;  // 0: 下架, 1: 上架
    private LocalDateTime inTime;   // 入库时间
    private LocalDateTime outTime;  // 出库时间
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
```

### 2.6 分类控制器

**文件路径**: `controller/CategoryController.java`

```1:53:src/main/java/com/swiftstock/controller/CategoryController.java
package com.swiftstock.controller;

import com.swiftstock.dto.Result;
import com.swiftstock.entity.Category;
import com.swiftstock.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

/**
 * 商品分类管理控制器
 *
 * <p>提供分类的查询接口，用于商品管理中的分类展示。</p>
 */
@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类列表
     */
    @GetMapping
    public ResponseEntity<Result<List<Category>>> getCategories() {
        try {
            List<Category> categories = categoryService.findAll();
            return ResponseEntity.ok(Result.ok(categories));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取分类列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<Category>> getCategory(@PathVariable Long id) {
        try {
            Category category = categoryService.findById(id);
            if (category == null) {
                return ResponseEntity.ok(Result.fail("分类不存在"));
            } else {
                return ResponseEntity.ok(Result.ok(category));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取分类详情失败：" + e.getMessage()));
        }
    }
}
```

### 2.7 分类实体

**文件路径**: `entity/Category.java`

```1:26:src/main/java/com/swiftstock/entity/Category.java
package com.swiftstock.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类实体
 *
 * <p>用于表示商品的分类层级信息，支持父子关系与路径表示，供分类管理与商品关联使用。</p>
 */
@Data
public class Category {
    private Long id;
    private String name;
    private Long parentId;        // 父级分类ID
    private Integer level;         // 分类层级：1-一级分类，2-二级分类，3-三级分类
    private Integer sortOrder;    // 排序字段
    private String description;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    
    // 关联字段
    private List<Category> children;  // 子分类列表
    private String fullPath;          // 完整分类路径，如：服装-女装-连衣裙
}
```

---

## 三、订单管理模块

### 3.1 模块概述

订单管理模块是电商系统的核心业务模块，负责订单的创建、查询、状态流转、取消、删除等功能。模块实现了完整的状态机模型，支持订单从创建到完成的全生命周期管理，并与库存系统紧密集成保证数据一致性。

### 3.2 核心类清单

| 类名 | 类型 | 职责 |
|------|------|------|
| `OrderController` | Controller | 订单管理 REST API |
| `OrderService` | Service | 订单业务接口 |
| `OrderServiceImpl` | Service | 订单业务实现 |
| `OrderMapper` | Mapper | 订单数据访问 |
| `OrderItemMapper` | Mapper | 订单项数据访问 |
| `OrderStatusHistoryMapper` | Mapper | 订单状态历史数据访问 |
| `Order` | Entity | 订单实体 |
| `OrderItem` | Entity | 订单项实体 |
| `OrderStatus` | Entity | 订单状态枚举 |
| `OrderStatusHistory` | Entity | 订单状态历史实体 |
| `OrderCreateDTO` | DTO | 订单创建数据传输对象 |

### 3.3 订单控制器

**文件路径**: `controller/OrderController.java`

```1:219:src/main/java/com/swiftstock/controller/OrderController.java
package com.swiftstock.controller;

import com.swiftstock.dto.OrderCreateDTO;
import com.swiftstock.dto.Result;
import com.swiftstock.entity.Order;
import com.swiftstock.entity.OrderStatus;
import com.swiftstock.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单管理接口（Controller）
 *
 * <p>职责：
 * <ul>
 *   <li>提供订单的创建、查询、状态更新/流转、取消、删除等 REST API</li>
 *   <li>负责入参接收与基础校验，复杂业务规则下沉至 {@code OrderService}</li>
 *   <li>统一返回结构：{@code {success, message?, data?}}</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 获取订单列表
     *
     * <p>支持按订单号、客户名称、订单状态筛选。
     */
    @GetMapping
    public ResponseEntity<Result<Map<String, Object>>> getOrders(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            log.debug("Searching orders with params: orderNo={}, customerName={}, status={}", orderNo, customerName, status);

            Order searchParams = new Order();
            searchParams.setOrderNo(orderNo);
            searchParams.setCustomerName(customerName);
            if (status != null && !status.isEmpty()) {
                try {
                    searchParams.setStatus(OrderStatus.valueOf(status.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid order status: {}", status);
                }
            }

            List<Order> orders = orderService.findByCondition(searchParams);
            log.debug("Found {} orders", orders.size());

            // 简单分页处理
            int start = (page - 1) * size;
            int end = Math.min(start + size, orders.size());
            List<Order> pageOrders = orders.subList(start, end);

            Map<String, Object> data = new HashMap<>();
            data.put("list", pageOrders);
            data.put("total", orders.size());
            data.put("page", page);
            data.put("size", size);

            return ResponseEntity.ok(Result.ok(data));
        } catch (Exception e) {
            log.error("Failed to get order list", e);
            return ResponseEntity.ok(Result.fail("获取订单列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<Order>> getOrder(@PathVariable Long id) {
        try {
            Order order = orderService.findById(id);
            if (order == null) {
                return ResponseEntity.ok(Result.fail("订单不存在"));
            } else {
                return ResponseEntity.ok(Result.ok(order));
            }
        } catch (Exception e) {
            log.error("Failed to get order details. Order ID: {}", id, e);
            return ResponseEntity.ok(Result.fail("获取订单详情失败：" + e.getMessage()));
        }
    }

    /**
     * 创建订单
     */
    @PostMapping
    public ResponseEntity<Result<Order>> createOrder(@RequestBody OrderCreateDTO orderDTO) {
        try {
            Order order = orderService.createOrder(orderDTO);
            return ResponseEntity.ok(Result.ok(order, "订单创建成功"));
        } catch (Exception e) {
            log.error("Failed to create order", e);
            return ResponseEntity.ok(Result.fail("订单创建失败：" + e.getMessage()));
        }
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Result<Void>> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            log.debug("Updating order {} status to {}", id, status);

            orderService.updateStatus(id, status.toUpperCase());
            return ResponseEntity.ok(Result.ok(null, "订单状态更新成功"));
        } catch (Exception e) {
            log.error("Failed to update order status. Order ID: {}, Status: {}", id, request.get("status"), e);
            return ResponseEntity.ok(Result.fail("订单状态更新失败：" + e.getMessage()));
        }
    }

    /**
     * 订单状态流转（支持完整流程）
     */
    @PutMapping("/{id}/transition")
    public ResponseEntity<Result<Void>> transitionStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String targetStatus = request.get("status");
            String reason = request.get("reason");
            log.debug("Transitioning order {} to status {}", id, targetStatus);

            orderService.transitionStatus(id, targetStatus.toUpperCase(), reason);
            return ResponseEntity.ok(Result.ok(null, "订单状态流转成功"));
        } catch (Exception e) {
            log.error("Failed to transition order status. Order ID: {}, Status: {}", id, request.get("status"), e);
            return ResponseEntity.ok(Result.fail("订单状态流转失败：" + e.getMessage()));
        }
    }

    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Result<Void>> cancelOrder(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            log.debug("Cancelling order {} with reason: {}", id, reason);

            orderService.cancelOrder(id, reason);
            return ResponseEntity.ok(Result.ok(null, "订单取消成功"));
        } catch (Exception e) {
            log.error("Failed to cancel order. Order ID: {}", id, e);
            return ResponseEntity.ok(Result.fail("订单取消失败：" + e.getMessage()));
        }
    }

    /**
     * 获取订单状态流转历史
     */
    @GetMapping("/{id}/status-history")
    public ResponseEntity<Result<List<com.swiftstock.entity.OrderStatusHistory>>> getStatusHistory(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(Result.ok(orderService.getStatusHistory(id)));
        } catch (Exception e) {
            log.error("Failed to get order status history. Order ID: {}", id, e);
            return ResponseEntity.ok(Result.fail("获取状态历史失败：" + e.getMessage()));
        }
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteOrder(@PathVariable Long id) {
        try {
            log.debug("Deleting order: {}", id);

            orderService.deleteById(id);
            return ResponseEntity.ok(Result.ok(null, "订单删除成功"));
        } catch (Exception e) {
            log.error("Failed to delete order. Order ID: {}", id, e);
            return ResponseEntity.ok(Result.fail("删除订单失败：" + e.getMessage()));
        }
    }
}
```

### 3.4 订单服务实现（核心业务逻辑）

**文件路径**: `service/impl/OrderServiceImpl.java`

```1:263:src/main/java/com/swiftstock/service/impl/OrderServiceImpl.java
package com.swiftstock.service.impl;

import com.swiftstock.dto.OrderCreateDTO;
import com.swiftstock.entity.Order;
import com.swiftstock.entity.OrderItem;
import com.swiftstock.entity.OrderStatus;
import com.swiftstock.entity.OrderStatusHistory;
import com.swiftstock.entity.Product;
import com.swiftstock.mapper.OrderMapper;
import com.swiftstock.mapper.OrderItemMapper;
import com.swiftstock.mapper.OrderStatusHistoryMapper;
import com.swiftstock.service.OrderService;
import com.swiftstock.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单业务实现（Service）
 *
 * <p>核心点：
 * <ul>
 *   <li>订单创建：写入订单与订单项，并计算总金额</li>
 *   <li>订单状态机：通过 {@link com.swiftstock.entity.OrderStatus#canTransitionTo(OrderStatus)} 校验合法流转</li>
 *   <li>库存一致性：状态进入/退出"已付款"时触发库存扣减/恢复（避免超卖/少卖）</li>
 *   <li>事务控制：关键写操作使用 {@code @Transactional} 保证订单与库存变更原子性</li>
 * </ul>
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter ORDER_NO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderStatusHistoryMapper orderStatusHistoryMapper;

    @Override
    @Transactional
    public Order createOrder(OrderCreateDTO orderDTO) {
        // 1) 创建订单主表（orders）
        Order order = new Order();
        if (orderDTO.getOrderNo() != null && !orderDTO.getOrderNo().isEmpty()) {
            order.setOrderNo(orderDTO.getOrderNo());
        } else {
            order.setOrderNo(generateOrderNo());
        }
        order.setCustomerName(orderDTO.getCustomerName());
        order.setCustomerPhone(orderDTO.getCustomerPhone());
        order.setRemark(orderDTO.getRemark());
        if (orderDTO.getStatus() != null && !orderDTO.getStatus().isEmpty()) {
            order.setStatus(OrderStatus.valueOf(orderDTO.getStatus().toUpperCase()));
        } else {
            order.setStatus(OrderStatus.UNPAID);
        }
        order.setCreatedTime(LocalDateTime.now());
        
        // 2) 处理订单项并累计总金额
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderCreateDTO.OrderItemDTO itemDTO : orderDTO.getItems()) {
            OrderItem item = new OrderItem();
            item.setProductId(itemDTO.getProductId());
            item.setProductName(itemDTO.getProductName());
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(BigDecimal.valueOf(itemDTO.getPrice()));
            item.setAmount(BigDecimal.valueOf(itemDTO.getAmount()));
            item.setCreatedTime(LocalDateTime.now());
            
            orderItems.add(item);
            totalAmount = totalAmount.add(item.getAmount());
        }
        
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        
        // 3) 保存订单与订单项
        orderMapper.insert(order);
        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
            
            // 4) 更新库存（只有在订单状态为 PAID 时才扣减库存）
            if (order.getStatus() == OrderStatus.PAID) {
                productService.updateStock(item.getProductId(), -item.getQuantity());
                log.info("订单{}状态为已付款，自动扣减商品{}库存{}件", order.getOrderNo(), item.getProductName(), item.getQuantity());
            }
        }

        // 5) 记录初始状态历史
        logStatusHistory(null, order.getStatus(), "订单创建", order);

        return order;
    }

    /**
     * 生成订单编号（格式：ORD + yyyyMMdd + 4位序号）
     */
    private String generateOrderNo() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(ORDER_NO_DATE_FORMATTER);
        String prefix = "ORD" + datePart;
        
        String maxOrderNo = orderMapper.selectMaxOrderNoByPrefix(prefix);
        
        int nextSequence = 1;
        if (maxOrderNo != null && maxOrderNo.length() >= prefix.length() + 4) {
            try {
                String seqStr = maxOrderNo.substring(prefix.length());
                int currentSeq = Integer.parseInt(seqStr);
                nextSequence = currentSeq + 1;
            } catch (Exception e) {
                log.warn("解析订单号失败：{}，将从0001开始", maxOrderNo, e);
            }
        }
        
        String sequencePart = String.format("%04d", nextSequence);
        return prefix + sequencePart;
    }

    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    @Override
    public List<Order> findByCondition(Order searchParams) {
        try {
            log.debug("Finding orders by condition: {}", searchParams);
            if (searchParams == null) {
                return findAll();
            }
            List<Order> orders = orderMapper.findByCondition(searchParams);
            log.debug("Found {} orders matching the condition", orders.size());
            return orders;
        } catch (Exception e) {
            log.error("Error finding orders by condition: {}", searchParams, e);
            throw new RuntimeException("查询订单失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Order findById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status) {
        Order order = findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在：" + id);
        }
        
        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = OrderStatus.valueOf(status);
        
        // 1) 检查状态流转是否合法
        if (!oldStatus.canTransitionTo(newStatus)) {
            throw new RuntimeException(String.format("订单状态不能从%s流转到%s", 
                oldStatus.getDescription(), newStatus.getDescription()));
        }
        
        // 2) 业务逻辑验证
        validateStatusTransition(order, oldStatus, newStatus);
        
        // 3) 处理状态变更时的库存操作
        if (oldStatus != newStatus) {
            handleStockOperationOnStatusChange(order, oldStatus, newStatus);
        }
        
        order.setStatus(newStatus);
        order.setUpdatedTime(LocalDateTime.now());
        orderMapper.updateStatus(order);

        logStatusHistory(oldStatus, newStatus, "状态更新", order);
        
        log.info("订单{}状态从{}变更为{}", order.getOrderNo(), oldStatus.getDescription(), newStatus.getDescription());
    }
    
    /**
     * 处理订单状态变更时的库存操作
     */
    private void handleStockOperationOnStatusChange(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        // 场景A：进入"已付款" => 扣减库存
        if (newStatus == OrderStatus.PAID && oldStatus != OrderStatus.PAID) {
            for (OrderItem item : order.getItems()) {
                productService.updateStock(item.getProductId(), -item.getQuantity());
                log.info("订单{}状态变更为已付款，自动扣减商品{}库存{}件", order.getOrderNo(), item.getProductName(), item.getQuantity());
            }
        }
        // 场景B：从"已付款"离开到非"已完成" => 恢复库存
        else if (oldStatus == OrderStatus.PAID && newStatus != OrderStatus.COMPLETED && newStatus != OrderStatus.PAID) {
            for (OrderItem item : order.getItems()) {
                productService.updateStock(item.getProductId(), item.getQuantity());
                log.info("订单{}状态从已付款变更为{}，自动恢复商品{}库存{}件", order.getOrderNo(), newStatus.getDescription(), item.getProductName(), item.getQuantity());
            }
        }
    }

    @Override
    @Transactional
    public void transitionStatus(Long id, String targetStatus, String reason) {
        Order order = findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在：" + id);
        }
        
        OrderStatus currentStatus = order.getStatus();
        OrderStatus newStatus = OrderStatus.valueOf(targetStatus);
        
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new RuntimeException(String.format("订单状态不能从%s流转到%s", 
                currentStatus.getDescription(), newStatus.getDescription()));
        }
        
        validateStatusTransition(order, currentStatus, newStatus);
        handleStockOperationOnStatusChange(order, currentStatus, newStatus);
        
        order.setStatus(newStatus);
        order.setUpdatedTime(LocalDateTime.now());
        orderMapper.updateStatus(order);

        logStatusHistory(currentStatus, newStatus, reason, order);
        
        log.info("订单{}状态流转：{} -> {}，原因：{}", order.getOrderNo(), 
                currentStatus.getDescription(), newStatus.getDescription(), reason);
    }
    
    /**
     * 验证状态流转的业务逻辑
     */
    private void validateStatusTransition(Order order, OrderStatus currentStatus, OrderStatus newStatus) {
        if (newStatus.requiresStockValidation()) {
            validateStockForPreparing(order);
        }
        
        if (newStatus == OrderStatus.SHIPPED) {
            validateForShipping(order);
        }
        
        if (newStatus == OrderStatus.COMPLETED) {
            validateForCompletion(order);
        }
        
        if (newStatus == OrderStatus.CANCELLED) {
            validateForCancellation(order, currentStatus);
        }
        
        if (newStatus == OrderStatus.REFUNDED) {
            validateForRefund(order, currentStatus);
        }
    }
    
    private void validateStockForPreparing(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = productService.findById(item.getProductId());
            if (product == null) {
                throw new RuntimeException(String.format("商品%s不存在", item.getProductName()));
            }
            
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException(String.format("商品%s库存不足，当前库存：%d，需要：%d", 
                    item.getProductName(), product.getStockQuantity(), item.getQuantity()));
            }
        }
        log.info("订单{}配货前库存检查通过", order.getOrderNo());
    }
    
    private void validateForShipping(Order order) {
        if (order.getStatus() != OrderStatus.PAID && order.getStatus() != OrderStatus.PREPARING) {
            throw new RuntimeException("只有已付款或配货中的订单才能发货");
        }
        if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty()) {
            throw new RuntimeException("订单缺少客户信息，无法发货");
        }
        log.info("订单{}发货前业务检查通过", order.getOrderNo());
    }
    
    private void validateForCompletion(Order order) {
        if (order.getStatus() != OrderStatus.SHIPPED && order.getStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("只有已发货或已送达的订单才能完成");
        }
        log.info("订单{}完成前业务检查通过", order.getOrderNo());
    }
    
    private void validateForCancellation(Order order, OrderStatus currentStatus) {
        if (!currentStatus.canBeCancelled()) {
            throw new RuntimeException(String.format("订单状态为%s，无法取消", currentStatus.getDescription()));
        }
        log.info("订单{}取消前业务检查通过", order.getOrderNo());
    }
    
    private void validateForRefund(Order order, OrderStatus currentStatus) {
        if (!currentStatus.canBeRefunded()) {
            throw new RuntimeException(String.format("订单状态为%s，无法退款", currentStatus.getDescription()));
        }
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("订单金额无效，无法退款");
        }
        log.info("订单{}退款前业务检查通过", order.getOrderNo());
    }

    @Override
    @Transactional
    public void cancelOrder(Long id, String reason) {
        Order order = findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在：" + id);
        }
        
        OrderStatus currentStatus = order.getStatus();
        
        if (currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            throw new RuntimeException("订单已完成或已取消，无法再次取消");
        }
        
        if (currentStatus == OrderStatus.PAID || currentStatus == OrderStatus.PREPARING || 
            currentStatus == OrderStatus.SHIPPED || currentStatus == OrderStatus.DELIVERED) {
            for (OrderItem item : order.getItems()) {
                productService.updateStock(item.getProductId(), item.getQuantity());
                log.info("订单{}取消，恢复商品{}库存{}件", order.getOrderNo(), item.getProductName(), item.getQuantity());
            }
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedTime(LocalDateTime.now());
        orderMapper.updateStatus(order);

        logStatusHistory(currentStatus, OrderStatus.CANCELLED, reason, order);
        
        log.info("订单{}已取消，原因：{}", order.getOrderNo(), reason);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Order order = findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在：" + id);
        }
        
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.PREPARING || 
            order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            for (OrderItem item : order.getItems()) {
                productService.updateStock(item.getProductId(), item.getQuantity());
                log.info("删除订单{}，恢复商品{}库存{}件", order.getOrderNo(), item.getProductName(), item.getQuantity());
            }
        }
        
        orderItemMapper.deleteByOrderId(id);
        orderMapper.deleteById(id);
        
        log.info("订单{}已删除", order.getOrderNo());
    }

    @Override
    public List<OrderStatusHistory> getStatusHistory(Long id) {
        Order order = findById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在：" + id);
        }
        return orderStatusHistoryMapper.findByOrderId(id);
    }

    private void logStatusHistory(OrderStatus fromStatus, OrderStatus toStatus, String reason, Order order) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrderId(order.getId());
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setReason(reason);
        history.setChangedTime(LocalDateTime.now());
        orderStatusHistoryMapper.insert(history);
    }
}
```

### 3.5 订单状态枚举（状态机核心）

**文件路径**: `entity/OrderStatus.java`

```java
// 订单状态枚举定义
public enum OrderStatus {
    UNPAID("待付款"),
    PAID("已付款"),
    PREPARING("配货中"),
    SHIPPED("已发货"),
    DELIVERED("已送达"),
    COMPLETED("已完成"),
    CANCELLED("已取消"),
    REFUNDED("已退款");

    // 状态机规则：是否可以流转到目标状态
    public boolean canTransitionTo(OrderStatus target) {
        // 定义允许的状态流转规则
        switch (this) {
            case UNPAID:
                return target == PAID || target == CANCELLED;
            case PAID:
                return target == PREPARING || target == CANCELLED || target == REFUNDED;
            case PREPARING:
                return target == SHIPPED || target == CANCELLED;
            case SHIPPED:
                return target == DELIVERED || target == REFUNDED;
            case DELIVERED:
                return target == COMPLETED || target == REFUNDED;
            default:
                return false;
        }
    }

    // 是否需要库存校验
    public boolean requiresStockValidation() {
        return this == PREPARING;
    }

    // 是否可以取消
    public boolean canBeCancelled() {
        return this == UNPAID || this == PAID || this == PREPARING;
    }

    // 是否可以退款
    public boolean canBeRefunded() {
        return this == PAID || this == PREPARING || this == SHIPPED || this == DELIVERED || this == COMPLETED;
    }
}
```

### 3.6 订单相关实体

**文件路径**: `entity/Order.java`

```1:38:src/main/java/com/swiftstock/entity/Order.java
package com.swiftstock.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单实体
 */
@Data
public class Order {
    private Long id;
    private String orderNo;
    private String customerName;
    private String customerPhone;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String remark;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private List<OrderItem> items = new ArrayList<>();
    
    // 非数据库字段
    private Integer itemCount;
    private String startDate;
    private String endDate;
    
    public Integer getItemCount() {
        if (items != null) {
            return items.stream()
                    .mapToInt(OrderItem::getQuantity)
                    .sum();
        }
        return 0;
    }
}
```

**文件路径**: `entity/OrderItem.java`

```1:29:src/main/java/com/swiftstock/entity/OrderItem.java
package com.swiftstock.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单项实体
 */
@Data
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal amount;
    private String remark;
    private LocalDateTime createdTime;
    private Product product;  // 关联的商品信息
    
    public BigDecimal getAmount() {
        if (price != null && quantity != null) {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
}
```

---

## 四、供应商管理模块

### 4.1 模块概述

供应商管理模块负责供应商信息的 CRUD 操作、供货记录管理，支持按名称与联系人筛选、分页查询等功能。

### 4.2 核心类清单

| 类名 | 类型 | 职责 |
|------|------|------|
| `SupplierController` | Controller | 供应商管理 REST API |
| `SupplyRecordController` | Controller | 供货记录 REST API |
| `SupplierService` | Service | 供应商业务接口 |
| `SupplierServiceImpl` | Service | 供应商业务实现 |
| `SupplyRecordService` | Service | 供货记录业务接口 |
| `SupplyRecordServiceImpl` | Service | 供货记录业务实现 |
| `SupplierMapper` | Mapper | 供应商数据访问 |
| `SupplyRecordMapper` | Mapper | 供货记录数据访问 |
| `Supplier` | Entity | 供应商实体 |
| `SupplyRecord` | Entity | 供货记录实体 |

### 4.3 供应商控制器

**文件路径**: `controller/SupplierController.java`

```1:133:src/main/java/com/swiftstock/controller/SupplierController.java
package com.swiftstock.controller;

import com.swiftstock.dto.Result;
import com.swiftstock.entity.Supplier;
import com.swiftstock.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 供应商管理控制器
 *
 * <p>职责：
 * <ul>
 *   <li>提供供应商的增删改查与分页查询接口，支持按名称与联系人筛选。</li>
 *   <li>提供获取单个供应商详情与返回全部供应商的便捷接口。</li>
 *   <li>提供查询供应商对应供货记录的接口。</li>
 * </ul>
 */
@RestController
@RequestMapping("/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    /**
     * 分页查询供应商
     */
    @GetMapping
    public ResponseEntity<Result<Map<String, Object>>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String contactPerson,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        int p = page != null && page > 0 ? page : 1;
        int s = size != null && size > 0 ? size : 10;
        int offset = (p - 1) * s;

        List<Supplier> list = supplierService.findPage(name, contactPerson, offset, s);
        long total = supplierService.count(name, contactPerson);

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total);
        data.put("page", p);
        data.put("size", s);

        return ResponseEntity.ok(Result.ok(data));
    }

    /**
     * 根据ID查询供应商
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<Supplier>> getById(@PathVariable Long id) {
        Supplier s = supplierService.findById(id);
        return ResponseEntity.ok(Result.ok(s));
    }

    /**
     * 返回全部供应商
     */
    @GetMapping("/all")
    public ResponseEntity<Result<List<Supplier>>> listAll() {
        List<Supplier> list = supplierService.findAll();
        return ResponseEntity.ok(Result.ok(list));
    }

    /**
     * 根据ID查询供应商供货记录
     */
    @GetMapping("/{id}/records")
    public ResponseEntity<Result<List<com.swiftstock.entity.SupplyRecord>>> getRecords(@PathVariable Long id) {
        List<com.swiftstock.entity.SupplyRecord> records = supplierService instanceof com.swiftstock.service.impl.SupplierServiceImpl
                ? ((com.swiftstock.service.impl.SupplierServiceImpl) supplierService).findRecords(id)
                : java.util.Collections.emptyList();
        return ResponseEntity.ok(Result.ok(records));
    }

    /**
     * 创建供应商
     */
    @PostMapping
    public ResponseEntity<Result<Boolean>> create(@RequestBody Supplier supplier) {
        boolean ok = supplierService.create(supplier);
        if (ok) {
            return ResponseEntity.ok(Result.ok(true, "创建成功"));
        } else {
            return ResponseEntity.ok(Result.fail("创建失败"));
        }
    }

    /**
     * 更新供应商
     */
    @PutMapping("/{id}")
    public ResponseEntity<Result<Boolean>> update(@PathVariable Long id, @RequestBody Supplier supplier) {
        supplier.setId(id);
        boolean ok = supplierService.update(supplier);
        if (ok) {
            return ResponseEntity.ok(Result.ok(true, "更新成功"));
        } else {
            return ResponseEntity.ok(Result.fail("更新失败"));
        }
    }

    /**
     * 删除供应商
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Boolean>> delete(@PathVariable Long id) {
        boolean ok = supplierService.deleteById(id);
        if (ok) {
            return ResponseEntity.ok(Result.ok(true, "删除成功"));
        } else {
            return ResponseEntity.ok(Result.fail("删除失败"));
        }
    }
}
```

### 4.4 供货记录控制器

**文件路径**: `controller/SupplyRecordController.java`

```1:161:src/main/java/com/swiftstock/controller/SupplyRecordController.java
package com.swiftstock.controller;

import com.swiftstock.dto.Result;
import com.swiftstock.dto.SupplyRecordDTO;
import com.swiftstock.entity.SupplyRecord;
import com.swiftstock.service.SupplyRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 控制器：供货记录（SupplyRecord）CRUD 操作
 *
 * <p>提供分页查询、创建、更新、删除接口，统一返回 {@link com.swiftstock.dto.Result} 结构。</p>
 */
@RestController
@RequestMapping("/supply-records")
public class SupplyRecordController {

	@Autowired
	private SupplyRecordService supplyRecordService;

	/**
	 * 分页查询指定供应商的供货记录
	 */
	@GetMapping
	public ResponseEntity<Result<Map<String, Object>>> list(
			@RequestParam Long supplierId,
			@RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer size) {
		try {
			int p = Math.max(1, page);
			int s = Math.max(1, size);
			int offset = (p - 1) * s;
			List<SupplyRecord> list = supplyRecordService.findPage(supplierId, offset, s);
			long total = supplyRecordService.countBySupplierId(supplierId);
			Map<String, Object> data = new HashMap<>();
			data.put("list", list);
			data.put("total", total);
			return ResponseEntity.ok(Result.ok(data));
		} catch (Exception e) {
			return ResponseEntity.ok(Result.fail("查询供货记录失败：" + e.getMessage()));
		}
	}

	/**
	 * 创建供货记录
	 */
	@PostMapping
	public ResponseEntity<Result<Boolean>> create(@RequestBody SupplyRecordDTO dto) {
		try {
			SupplyRecord record = new SupplyRecord();
			record.setSupplierId(dto.getSupplierId());
			record.setProductId(dto.getProductId());
			record.setProductName(dto.getProductName());
			record.setQuantity(dto.getQuantity());
			record.setUnitPrice(dto.getUnitPrice());
			record.setRemark(dto.getRemark());

			if (dto.getReceiveTime() != null && !dto.getReceiveTime().trim().isEmpty()) {
				try {
					DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					LocalDateTime rt = LocalDateTime.parse(dto.getReceiveTime(), fmt);
					record.setReceiveTime(rt);
				} catch (Exception pe) {
					return ResponseEntity.ok(Result.fail("接收时间格式错误，期望 yyyy-MM-dd HH:mm:ss"));
				}
			}

			boolean ok = supplyRecordService.create(record);
			if (ok) {
				return ResponseEntity.ok(Result.ok(true));
			} else {
				return ResponseEntity.ok(Result.fail("创建失败"));
			}
		} catch (Exception e) {
			return ResponseEntity.ok(Result.fail("创建供货记录失败：" + e.getMessage()));
		}
	}

	/**
	 * 更新供货记录
	 */
	@PutMapping("/{id}")
	public ResponseEntity<Result<Boolean>> update(@PathVariable Long id, @RequestBody SupplyRecordDTO dto) {
		try {
			SupplyRecord record = new SupplyRecord();
			record.setId(id);
			record.setSupplierId(dto.getSupplierId());
			record.setProductId(dto.getProductId());
			record.setProductName(dto.getProductName());
			record.setQuantity(dto.getQuantity());
			record.setUnitPrice(dto.getUnitPrice());
			record.setRemark(dto.getRemark());

			boolean ok = supplyRecordService.update(record);
			if (ok) {
				return ResponseEntity.ok(Result.ok(true));
			} else {
				return ResponseEntity.ok(Result.fail("更新失败"));
			}
		} catch (Exception e) {
			return ResponseEntity.ok(Result.fail("更新供货记录失败：" + e.getMessage()));
		}
	}

	/**
	 * 删除供货记录
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Result<Boolean>> delete(@PathVariable Long id) {
		try {
			boolean ok = supplyRecordService.deleteById(id);
			if (ok) {
				return ResponseEntity.ok(Result.ok(true));
			} else {
				return ResponseEntity.ok(Result.fail("删除失败"));
			}
		} catch (Exception e) {
			return ResponseEntity.ok(Result.fail("删除供货记录失败：" + e.getMessage()));
		}
	}
}
```

### 4.5 供应商与供货记录实体

**文件路径**: `entity/Supplier.java`

```1:21:src/main/java/com/swiftstock/entity/Supplier.java
package com.swiftstock.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 供应商实体
 */
@Data
public class Supplier {
    private Long id;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String remark;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
```

**文件路径**: `entity/SupplyRecord.java`

```1:21:src/main/java/com/swiftstock/entity/SupplyRecord.java
package com.swiftstock.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 供货记录实体
 */
@Data
public class SupplyRecord {
    private Long id;
    private Long supplierId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private java.math.BigDecimal unitPrice;
    private java.math.BigDecimal totalAmount;
    private LocalDateTime receiveTime;
    private String remark;
}
```

---

## 五、库存管理模块

### 5.1 模块概述

库存管理模块负责商品库存的查询、入库、出库操作以及库存变动的追溯记录。模块设计遵循"商品表保存当前库存，记录表保存变动明细"的原则，确保库存数据的可追溯性。

### 5.2 核心类清单

| 类名 | 类型 | 职责 |
|------|------|------|
| `InventoryController` | Controller | 库存管理 REST API |
| `StockAlertController` | Controller | 库存预警 REST API |
| `InventoryService` | Service | 库存业务接口 |
| `InventoryServiceImpl` | Service | 库存业务实现 |
| `StockAlertService` | Service | 库存预警业务接口 |
| `StockAlertServiceImpl` | Service | 库存预警业务实现 |
| `InventoryRecordMapper` | Mapper | 库存记录数据访问 |
| `InventoryRecord` | Entity | 库存记录实体 |

### 5.3 库存控制器

**文件路径**: `controller/InventoryController.java`

```1:333:src/main/java/com/swiftstock/controller/InventoryController.java
package com.swiftstock.controller;

import com.swiftstock.dto.Result;
import com.swiftstock.entity.InventoryRecord;
import com.swiftstock.entity.Product;
import com.swiftstock.service.InventoryService;
import com.swiftstock.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * 库存管理接口（Controller）
 *
 * <p>职责：
 * <ul>
 *   <li>库存列表查询（支持条件筛选与分页）</li>
 *   <li>入库/出库操作（落库库存记录）</li>
 *   <li>库存操作类型封装（采购/退货/调拨/损耗等映射到入/出库）</li>
 * </ul>
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {
    
    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private ProductService productService;
    
    /**
     * 获取库存列表
     *
     * <p>stockStatus 约定：
     * <ul>
     *   <li>{@code out}：缺货（stock_quantity == 0）</li>
     *   <li>{@code low}：低库存（0 < stock_quantity <= min_stock_level）</li>
     *   <li>{@code normal}：正常库存（stock_quantity > min_stock_level）</li>
     * </ul>
     */
    @GetMapping
    public ResponseEntity<Result<Map<String, Object>>> getInventoryList(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String stockStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            logger.info("Accessing inventory list with params: productName={}, categoryId={}, stockStatus={}",
                       productName, categoryId, stockStatus);

            List<Product> products = productService.findAll();
            
            // 1) 应用搜索过滤：商品名称模糊匹配
            if (productName != null && !productName.trim().isEmpty()) {
                products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(productName.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 2) 分类筛选
            if (categoryId != null) {
                products = products.stream()
                    .filter(p -> p.getCategoryId() != null && p.getCategoryId().equals(categoryId))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            if (stockStatus != null && !stockStatus.trim().isEmpty()) {
                logger.info("Applying stock status filter: {}", stockStatus);
                products = products.stream()
                    .filter(p -> {
                        boolean matches = false;
                        switch (stockStatus.toLowerCase()) {
                            case "out":
                                matches = p.getStockQuantity() == 0;
                                break;
                            case "low":
                                matches = p.getStockQuantity() > 0 && p.getStockQuantity() <= p.getMinStockLevel();
                                break;
                            case "normal":
                                matches = p.getStockQuantity() > p.getMinStockLevel();
                                break;
                            default:
                                matches = true;
                        }
                        return matches;
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // 3) 简单分页处理
            int start = (page - 1) * size;
            int end = Math.min(start + size, products.size());
            List<Product> pageProducts = products.subList(start, end);
            
            Map<String, Object> data = new HashMap<>();
            data.put("list", pageProducts);
            data.put("total", products.size());
            data.put("page", page);
            data.put("size", size);

            return ResponseEntity.ok(Result.ok(data));
        } catch (Exception e) {
            logger.error("Error while retrieving product list", e);
            return ResponseEntity.ok(Result.fail("获取库存列表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取商品库存记录
     */
    @GetMapping("/records/{productId}")
    public ResponseEntity<Result<Map<String, Object>>> getInventoryRecords(@PathVariable Long productId) {
        try {
            logger.info("Accessing inventory record with product ID: {}", productId);

            Product product = productService.findById(productId);
            if (product == null) {
                logger.error("Product not found with ID: {}", productId);
                return ResponseEntity.ok(Result.fail("商品不存在"));
            }

            List<InventoryRecord> records = inventoryService.findByProductId(productId);
            if (records == null) {
                records = Collections.emptyList();
            }

            Map<String, Object> data = new HashMap<>();
            data.put("product", product);
            data.put("records", records);

            return ResponseEntity.ok(Result.ok(data));
        } catch (Exception e) {
            logger.error("Error while retrieving inventory records for product ID: " + productId, e);
            return ResponseEntity.ok(Result.fail("获取库存记录失败：" + e.getMessage()));
        }
    }

    /**
     * 商品入库
     */
    @PostMapping("/in")
    public ResponseEntity<Result<Void>> stockIn(@RequestBody Map<String, Object> request) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            String reason = request.get("reason").toString();

            logger.info("Processing stock in request - Product ID: {}, Quantity: {}", productId, quantity);

            if (quantity <= 0) {
                logger.warn("Invalid quantity for stock in: {}", quantity);
                return ResponseEntity.ok(Result.fail("入库数量必须大于0"));
            }

            inventoryService.addStock(productId, quantity, reason);

            return ResponseEntity.ok(Result.ok(null, "入库操作成功"));
        } catch (Exception e) {
            logger.error("Error processing stock in request", e);
            return ResponseEntity.ok(Result.fail("入库操作失败：" + e.getMessage()));
        }
    }

    /**
     * 商品出库
     */
    @PostMapping("/out")
    public ResponseEntity<Result<Void>> stockOut(@RequestBody Map<String, Object> request) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            String reason = request.get("reason").toString();

            logger.info("Processing stock out request - Product ID: {}, Quantity: {}", productId, quantity);

            if (quantity <= 0) {
                logger.warn("Invalid quantity for stock out: {}", quantity);
                return ResponseEntity.ok(Result.fail("出库数量必须大于0"));
            }

            inventoryService.reduceStock(productId, quantity, reason);

            return ResponseEntity.ok(Result.ok(null, "出库操作成功"));
        } catch (Exception e) {
            logger.error("Error processing stock out request", e);
            return ResponseEntity.ok(Result.fail("出库操作失败：" + e.getMessage()));
        }
    }

    /**
     * 库存操作（支持多种操作类型）
     *
     * <p>该接口是"业务动作"到"库存增减"的映射层：
     * <ul>
     *   <li>purchase/return/transfer_in => 入库</li>
     *   <li>sales/transfer_out/damage => 出库</li>
     * </ul>
     */
    @PostMapping("/operation")
    public ResponseEntity<Result<Void>> stockOperation(@RequestBody Map<String, Object> request) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            String reason = request.get("reason").toString();
            String operationType = request.get("operationType").toString();

            logger.info("Processing stock operation - Product ID: {}, Quantity: {}, Type: {}",
                       productId, quantity, operationType);

            if (quantity <= 0) {
                logger.warn("Invalid quantity for stock operation: {}", quantity);
                return ResponseEntity.ok(Result.fail("操作数量必须大于0"));
            }

            switch (operationType) {
                case "purchase":
                case "return":
                case "transfer_in":
                    inventoryService.addStock(productId, quantity, reason);
                    break;
                case "sales":
                case "transfer_out":
                case "damage":
                    inventoryService.reduceStock(productId, quantity, reason);
                    break;
                default:
                    return ResponseEntity.ok(Result.fail("不支持的操作类型"));
            }

            return ResponseEntity.ok(Result.ok(null, "库存操作成功"));
        } catch (Exception e) {
            logger.error("Error processing stock operation", e);
            return ResponseEntity.ok(Result.fail("库存操作失败：" + e.getMessage()));
        }
    }

    /**
     * 批量库存操作
     */
    @PostMapping("/batch-operation")
    public ResponseEntity<Result<Void>> batchStockOperation(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> productIds = (List<Long>) request.get("productIds");
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            String reason = request.get("reason").toString();
            String operationType = request.get("operationType").toString();

            logger.info("Processing batch stock operation - Products: {}, Quantity: {}, Type: {}",
                       productIds.size(), quantity, operationType);

            if (quantity <= 0) {
                logger.warn("Invalid quantity for batch operation: {}", quantity);
                return ResponseEntity.ok(Result.fail("操作数量必须大于0"));
            }

            int successCount = 0;
            for (Long productId : productIds) {
                try {
                    switch (operationType) {
                        case "purchase":
                        case "return":
                        case "transfer_in":
                            inventoryService.addStock(productId, quantity, reason);
                            break;
                        case "sales":
                        case "transfer_out":
                        case "damage":
                            inventoryService.reduceStock(productId, quantity, reason);
                            break;
                    }
                    successCount++;
                } catch (Exception e) {
                    logger.warn("Failed to process product ID {}: {}", productId, e.getMessage());
                }
            }

            return ResponseEntity.ok(Result.ok(null, String.format("批量操作完成，成功处理 %d/%d 个商品", successCount, productIds.size())));
        } catch (Exception e) {
            logger.error("Error processing batch stock operation", e);
            return ResponseEntity.ok(Result.fail("批量操作失败：" + e.getMessage()));
        }
    }
}
```

### 5.4 库存服务实现

**文件路径**: `service/impl/InventoryServiceImpl.java`

```1:130:src/main/java/com/swiftstock/service/impl/InventoryServiceImpl.java
package com.swiftstock.service.impl;

import com.swiftstock.entity.InventoryRecord;
import com.swiftstock.mapper.InventoryRecordMapper;
import com.swiftstock.service.InventoryService;
import com.swiftstock.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 库存服务实现（Service）
 *
 * <p>设计思路：
 * <ul>
 *   <li>库存"当前值"保存在商品表 {@code product.stock_quantity}</li>
 *   <li>库存"变动明细"保存在 {@code inventory_record}，用于追溯与审计</li>
 *   <li>先更新商品库存，再写入库存记录；两者在事务内保证一致</li>
 * </ul>
 */
@Slf4j
@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRecordMapper inventoryRecordMapper;

    @Autowired
    private ProductService productService;

    /**
     * 查询指定商品的库存变动历史
     */
    @Override
    public List<InventoryRecord> findByProductId(Long productId) {
        log.debug("Finding inventory records for product ID: {}", productId);
        try {
            List<InventoryRecord> records = inventoryRecordMapper.findByProductId(productId);
            log.debug("Found {} records for product ID: {}", records.size(), productId);
            return records;
        } catch (Exception e) {
            log.error("Error finding inventory records for product ID: {}", productId, e);
            throw e;
        }
    }

    /**
     * 为商品增加库存并写入库存记录（入库）
     *
     * <p>流程：先更新商品库存，再写入一条 type=IN 的库存记录；方法在事务中执行。
     */
    @Override
    @Transactional
    public void addStock(Long productId, Integer quantity, String reason) {
        log.debug("Adding stock for product ID: {}, quantity: {}, reason: {}", productId, quantity, reason);
        if (quantity <= 0) {
            log.error("Invalid quantity: {}", quantity);
            throw new IllegalArgumentException("入库数量必须大于0");
        }

        try {
            // 1) 更新商品库存：product.stock_quantity += quantity
            productService.updateStock(productId, quantity);

            // 2) 写入库存记录：inventory_record(type=IN)
            InventoryRecord record = new InventoryRecord();
            record.setProductId(productId);
            record.setQuantity(quantity);
            record.setType("IN");
            record.setReason(reason);
            inventoryRecordMapper.insert(record);
            log.debug("Successfully added stock for product ID: {}", productId);
        } catch (Exception e) {
            log.error("Error adding stock for product ID: {}", productId, e);
            throw e;
        }
    }

    /**
     * 为商品减少库存并写入库存记录（出库）
     *
     * <p>流程：先更新商品库存（会校验负库存），再写入一条 type=OUT 的库存记录；方法在事务中执行。
     */
    @Override
    @Transactional
    public void reduceStock(Long productId, Integer quantity, String reason) {
        log.debug("Reducing stock for product ID: {}, quantity: {}, reason: {}", productId, quantity, reason);
        if (quantity <= 0) {
            log.error("Invalid quantity: {}", quantity);
            throw new IllegalArgumentException("出库数量必须大于0");
        }

        try {
            // 1) 更新商品库存：product.stock_quantity -= quantity
            productService.updateStock(productId, -quantity);

            // 2) 写入库存记录：inventory_record(type=OUT)
            InventoryRecord record = new InventoryRecord();
            record.setProductId(productId);
            record.setQuantity(quantity);
            record.setType("OUT");
            record.setReason(reason);
            inventoryRecordMapper.insert(record);
            log.debug("Successfully reduced stock for product ID: {}", productId);
        } catch (Exception e) {
            log.error("Error reducing stock for product ID: {}", productId, e);
            throw e;
        }
    }
}
```

### 5.5 库存预警控制器

**文件路径**: `controller/StockAlertController.java`

```1:114:src/main/java/com/swiftstock/controller/StockAlertController.java
package com.swiftstock.controller;

import com.swiftstock.dto.Result;
import com.swiftstock.entity.Product;
import com.swiftstock.service.StockAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页库存预警控制器
 *
 * <p>预警规则（与 Service 保持一致）：
 * <ul>
 *   <li>低库存：0 < stock_quantity <= min_stock_level</li>
 *   <li>缺货：stock_quantity == 0</li>
 * </ul>
 */
@RestController
@RequestMapping("/stock-alert")
public class StockAlertController {
    
    private static final Logger logger = LoggerFactory.getLogger(StockAlertController.class);
    
    @Autowired
    private StockAlertService stockAlertService;
    
    /**
     * 获取库存预警信息
     *
     * <p>返回：低库存商品列表 + 低库存数量 + 缺货数量 + 总预警数量。
     */
    @GetMapping("/info")
    public ResponseEntity<Result<Map<String, Object>>> getStockAlertInfo() {
        try {
            logger.info("获取库存预警信息");

            List<Product> lowStockProducts = stockAlertService.checkAllStockAlerts();
            int lowStockCount = stockAlertService.getLowStockCount();
            int outOfStockCount = stockAlertService.getOutOfStockCount();

            Map<String, Object> data = new HashMap<>();
            data.put("lowStockProducts", lowStockProducts);
            data.put("lowStockCount", lowStockCount);
            data.put("outOfStockCount", outOfStockCount);
            data.put("totalAlertCount", lowStockCount + outOfStockCount);

            logger.info("库存预警信息获取成功：库存不足{}个，缺货{}个", lowStockCount, outOfStockCount);
            return ResponseEntity.ok(Result.ok(data));
        } catch (Exception e) {
            logger.error("获取库存预警信息失败", e);
            return ResponseEntity.ok(Result.fail("获取库存预警信息失败：" + e.getMessage()));
        }
    }
    
    /**
     * 检查指定商品的库存预警状态
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<Result<Map<String, Object>>> checkProductStockAlert(@PathVariable Long productId) {
        try {
            logger.info("检查商品{}的库存预警状态", productId);

            boolean isAlert = stockAlertService.checkStockAlert(productId);

            Map<String, Object> data = new HashMap<>();
            data.put("productId", productId);
            data.put("isAlert", isAlert);

            logger.info("商品{}库存预警检查完成，预警状态：{}", productId, isAlert);
            return ResponseEntity.ok(Result.ok(data));
        } catch (Exception e) {
            logger.error("检查商品{}库存预警状态失败", productId, e);
            return ResponseEntity.ok(Result.fail("检查库存预警状态失败：" + e.getMessage()));
        }
    }
    
    /**
     * 获取库存预警统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Result<Map<String, Object>>> getStockAlertStats() {
        try {
            logger.info("获取库存预警统计");

            int lowStockCount = stockAlertService.getLowStockCount();
            int outOfStockCount = stockAlertService.getOutOfStockCount();

            Map<String, Object> data = new HashMap<>();
            data.put("lowStockCount", lowStockCount);
            data.put("outOfStockCount", outOfStockCount);
            data.put("totalAlertCount", lowStockCount + outOfStockCount);

            logger.info("库存预警统计获取成功：库存不足{}个，缺货{}个", lowStockCount, outOfStockCount);
            return ResponseEntity.ok(Result.ok(data));
        } catch (Exception e) {
            logger.error("获取库存预警统计失败", e);
            return ResponseEntity.ok(Result.fail("获取库存预警统计失败：" + e.getMessage()));
        }
    }
}
```

### 5.6 库存记录实体

**文件路径**: `entity/InventoryRecord.java`

```1:18:src/main/java/com/swiftstock/entity/InventoryRecord.java
package com.swiftstock.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 库存记录实体
 */
@Data
public class InventoryRecord {
    private Long id;
    private Long productId;
    private String productName;
    private String type;  // IN-入库，OUT-出库
    private Integer quantity;
    private String reason;
    private LocalDateTime createdTime;
}
```

---

## 六、数据统计模块

### 6.1 模块概述

数据统计模块负责系统的销售数据统计、报表生成、仪表盘数据提供等功能，为管理决策提供数据支持。

### 6.2 核心类清单

| 类名 | 类型 | 职责 |
|------|------|------|
| `SalesController` | Controller | 销售统计 REST API |
| `ReportController` | Controller | 报表 REST API |
| `SalesService` | Service | 销售业务接口 |
| `SalesServiceImpl` | Service | 销售业务实现 |
| `SalesMapper` | Mapper | 销售数据访问 |

### 6.3 销售统计控制器

**文件路径**: `controller/SalesController.java`

```1:69:src/main/java/com/swiftstock/controller/SalesController.java
package com.swiftstock.controller;

import com.swiftstock.dto.Result;
import com.swiftstock.service.SalesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 首页销售统计接口（Controller）
 *
 * <p>面向"经营分析"的数据接口，供前端折线图/概览卡片使用。
 * <p>数据来源：基于订单（PAID/COMPLETED）聚合计算（见 {@code SalesServiceImpl}）。
 */
@Slf4j
@RestController
@RequestMapping("/sales")
public class SalesController {

    @Autowired
    private SalesService salesService;

    /**
     * 获取销售趋势数据
     *
     * <p>period 约定：
     * <ul>
     *   <li>7d：近 7 天</li>
     *   <li>30d：近 30 天</li>
     *   <li>90d：近 90 天</li>
     * </ul>
     */
    @GetMapping("/trend")
    public ResponseEntity<Result<List<Map<String, Object>>>> getSalesTrend(
            @RequestParam(defaultValue = "7d") String period) {
        try {
            log.debug("Getting sales trend for period: {}", period);

            List<Map<String, Object>> trendData = salesService.getSalesTrend(period);

            return ResponseEntity.ok(Result.ok(trendData));
        } catch (Exception e) {
            log.error("Failed to get sales trend", e);
            return ResponseEntity.ok(Result.fail("获取销售趋势失败：" + e.getMessage()));
        }
    }

    /**
     * 获取销售统计概览
     *
     * <p>典型指标：总销售额、今日销售额、本月销售额、订单数量等。
     */
    @GetMapping("/overview")
    public ResponseEntity<Result<Map<String, Object>>> getSalesOverview() {
        try {
            Map<String, Object> overview = salesService.getSalesOverview();

            return ResponseEntity.ok(Result.ok(overview));
        } catch (Exception e) {
            log.error("Failed to get sales overview", e);
            return ResponseEntity.ok(Result.fail("获取销售概览失败：" + e.getMessage()));
        }
    }
}
```

### 6.4 报表控制器

**文件路径**: `controller/ReportController.java`

```1:122:src/main/java/com/swiftstock/controller/ReportController.java
package com.swiftstock.controller;

import com.swiftstock.dto.Result;
import com.swiftstock.entity.Product;
import com.swiftstock.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

/**
 * 报表接口（Controller）
 *
 * <p>功能：
 * <ul>
 *   <li>库存报表：统计库存健康度（正常/低库存/缺货）、总库存价值、明细列表</li>
 *   <li>销售报表：当前实现包含演示数据（便于前端图表展示）；销售趋势/概览建议使用 /sales 接口</li>
 * </ul>
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ProductService productService;

    /**
     * 获取库存报表
     */
    @GetMapping("/stock")
    public ResponseEntity<Result<Map<String, Object>>> getStockReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long categoryId) {

        try {
            List<Product> products = productService.findAll();
            
            // 统计数据
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProducts", products.size());
            
            long normalStock = products.stream()
                .filter(p -> p.getStockQuantity() > p.getMinStockLevel())
                .count();
            stats.put("normalStock", normalStock);
            
            long lowStock = products.stream()
                .filter(p -> p.getStockQuantity() > 0 && p.getStockQuantity() <= p.getMinStockLevel())
                .count();
            stats.put("lowStock", lowStock);
            
            long outOfStock = products.stream()
                .filter(p -> p.getStockQuantity() == 0)
                .count();
            stats.put("outOfStock", outOfStock);
            
            // 计算库存价值
            BigDecimal totalValue = products.stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getStockQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> data = new HashMap<>();
            data.put("stats", stats);
            data.put("details", products);
            data.put("total", products.size());
            data.put("totalValue", totalValue);

            return ResponseEntity.ok(Result.ok(data));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取库存报表失败：" + e.getMessage()));
        }
    }

    /**
     * 获取销售报表
     */
    @GetMapping("/sales")
    public ResponseEntity<Result<Map<String, Object>>> getSalesReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            // 模拟销售数据
            Map<String, Object> data = new HashMap<>();
            data.put("totalSales", 125000.00);
            data.put("totalOrders", 89);
            data.put("averageOrderValue", 1404.49);
            
            // 模拟每日销售数据
            List<Map<String, Object>> dailySales = List.of(
                Map.of("date", "2025-01-01", "sales", 1200.00),
                Map.of("date", "2025-01-02", "sales", 2000.00),
                Map.of("date", "2025-01-03", "sales", 1500.00),
                Map.of("date", "2025-01-04", "sales", 3000.00),
                Map.of("date", "2025-01-05", "sales", 2500.00),
                Map.of("date", "2025-01-06", "sales", 1800.00),
                Map.of("date", "2025-01-07", "sales", 2200.00)
            );
            data.put("dailySales", dailySales);

            return ResponseEntity.ok(Result.ok(data));
        } catch (Exception e) {
            return ResponseEntity.ok(Result.fail("获取销售报表失败：" + e.getMessage()));
        }
    }
}
```

---

## 七、人工智能辅助模块

### 7.1 模块概述

人工智能辅助模块集成大语言模型（LLM），提供智能补货预测、商品详情自动生成等智能化功能，提升电商仓库管理效率。

### 7.2 核心类清单

| 类名 | 类型 | 职责 |
|------|------|------|
| `AiForecastController` | Controller | AI 预测 REST API |
| `AiProductController` | Controller | AI 商品生成 REST API |
| `AiForecastService` | Service | AI 预测业务接口 |
| `AiForecastServiceImpl` | Service | AI 预测业务实现 |
| `AiProductService` | Service | AI 商品业务接口 |
| `AiProductServiceImpl` | Service | AI 商品业务实现 |
| `AiReorderRecommendVO` | DTO | AI 补货推荐值对象 |
| `AiProductGenerateDTO` | DTO | AI 商品生成传输对象 |

### 7.3 AI 补货预测控制器

**文件路径**: `controller/AiForecastController.java`

```java
@RestController
@RequestMapping("/ai/forecast")
public class AiForecastController {

    @Autowired
    private AiForecastService aiForecastService;

    /**
     * 获取 AI 补货推荐列表
     */
    @GetMapping("/recommand")
    public ResponseEntity<Result<List<AiReorderRecommendVO>>> getReorderRecommendations() {
        try {
            List<AiReorderRecommendVO> recommendations = aiForecastService.getRecommendList();
            return ResponseEntity.ok(Result.ok(recommendations));
        } catch (Exception e) {
            log.error("获取AI补货推荐失败", e);
            return ResponseEntity.ok(Result.fail("获取AI补货推荐失败：" + e.getMessage()));
        }
    }

    /**
     * 获取需要补货的商品数量
     */
    @GetMapping("/count")
    public ResponseEntity<Result<Integer>> getRecommendCount() {
        try {
            int count = aiForecastService.getRecommendCount();
            return ResponseEntity.ok(Result.ok(count));
        } catch (Exception e) {
            log.error("获取AI补货数量失败", e);
            return ResponseEntity.ok(Result.fail("获取AI补货数量失败：" + e.getMessage()));
        }
    }
}
```

### 7.4 AI 补货预测服务实现

**文件路径**: `service/impl/AiForecastServiceImpl.java`

```1:263:src/main/java/com/swiftstock/service/impl/AiForecastServiceImpl.java
package com.swiftstock.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftstock.dto.AiReorderRecommendVO;
import com.swiftstock.entity.Product;
import com.swiftstock.mapper.ProductMapper;
import com.swiftstock.service.AiForecastService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * AI 补货预测服务实现类
 *
 * <p>实现包含：调用大模型、解析 JSON、缓存结果、定时刷新等具体逻辑。</p>
 */
@Slf4j
@Service
public class AiForecastServiceImpl implements AiForecastService {

    private final ProductMapper productMapper;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 内存缓存，缓存 key = "recommend_list"
    private final ConcurrentHashMap<String, List<AiReorderRecommendVO>> cache = new ConcurrentHashMap<>();
    private volatile Instant lastRefresh = Instant.EPOCH;
    private final long ttlSeconds = 60 * 30; // 30 分钟

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    public AiForecastServiceImpl(ProductMapper productMapper, ChatClient.Builder chatClientBuilder) {
        this.productMapper = productMapper;
        this.chatClient = chatClientBuilder.build();

        // 定时刷新缓存，避免频繁请求大模型
        this.scheduler.scheduleAtFixedRate(this::refreshCacheSilently, ttlSeconds, ttlSeconds, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    /**
     * 获取需要补货的商品数量
     */
    @Override
    public int getRecommendCount() {
        List<AiReorderRecommendVO> list = getRecommendList();
        return list.size();
    }

    /**
     * 获取补货建议列表
     *
     * <p>优先使用缓存，若缓存失效则重新调用模型生成推荐并缓存结果，最终按建议补货量降序并取前 10 条返回。</p>
     */
    @Override
    public List<AiReorderRecommendVO> getRecommendList() {
        List<AiReorderRecommendVO> cached = cache.get("recommend_list");
        if (cached != null && Instant.now().minusSeconds(ttlSeconds).isBefore(lastRefresh)) {
            return cached;
        }

        List<AiReorderRecommendVO> list = generateRecommendations();
        list.sort(Comparator.comparing(AiReorderRecommendVO::getSuggestReorderQuantity, 
            Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        List<AiReorderRecommendVO> top = list.size() > 10 ? new ArrayList<>(list.subList(0, 10)) : list;

        cache.put("recommend_list", top);
        lastRefresh = Instant.now();
        log.info("AI智能补货推荐计算结束");
        return top;
    }

    private void refreshCacheSilently() {
        try {
            List<AiReorderRecommendVO> newList = generateRecommendations();
            newList.sort(Comparator.comparing(AiReorderRecommendVO::getSuggestReorderQuantity, 
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
            List<AiReorderRecommendVO> top = newList.size() > 10 ? new ArrayList<>(newList.subList(0, 10)) : newList;
            cache.put("recommend_list", top);
            lastRefresh = Instant.now();
        } catch (Exception ignored) {
            // 保持静默，防止调度器中断
        }
    }

    /**
     * 异步处理单个商品的AI补货建议
     */
    @Async
    public CompletableFuture<AiReorderRecommendVO> processProductAsync(Product product) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (product.getStatus() == null || product.getStatus() != 1) {
                    return null;
                }

                String productName = product.getName();
                String productCode = product.getCode();
                Integer currentStock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
                Integer minStock = product.getMinStockLevel() == null ? 10 : product.getMinStockLevel();

                String prompt = ""
                        + "你是一名拥有10年经验的电商仓库管理专家。请根据以下商品信息，分析未来7天的销售情况并给出补货建议。\n\n"
                        + "商品名称：" + productName + "\n"
                        + "商品编码：" + productCode + "\n"
                        + "当前库存：" + currentStock + " 件\n"
                        + "最小库存阈值：" + minStock + " 件\n\n"
                        + "背景信息：\n"
                        + "- 本店主要销售运动服装鞋帽类商品\n"
                        + "- 周末销量通常是工作日的1.5-2倍\n"
                        + "- 近期无大型促销活动\n\n"
                        + "请直接输出纯JSON格式（不要任何其他文字）：\n"
                        + "{\n"
                        + "  \"needReorder\": true 或 false,\n"
                        + "  \"forecastSales7Days\": 整数（你预测的未来7天销量，合理范围10-100）, \n"
                        + "  \"suggestReorderQuantity\": 整数（建议补货数量，如果不需要补货填0，取10的倍数向上取整，最小补货单位10件）, \n"
                        + "  \"advice\": \"50-80字自然流畅的中文建议，语气专业，像人类专家写的一样，突出缺货风险、补货紧迫性和理由\"\n"
                        + "}\n";

                String aiResponse = chatClient.prompt()
                        .user(prompt)
                        .call()
                        .content();

                if (aiResponse == null || aiResponse.trim().isEmpty()) {
                    return null;
                }

                JsonNode root = objectMapper.readTree(aiResponse);
                if (root == null || !root.has("needReorder")) {
                    return null;
                }

                boolean needReorder = root.get("needReorder").asBoolean(false);
                if (!needReorder) {
                    return null;
                }

                int forecastSales7Days = root.has("forecastSales7Days") ? root.get("forecastSales7Days").asInt(0) : 0;
                int suggestReorderQuantity = root.has("suggestReorderQuantity") ? root.get("suggestReorderQuantity").asInt(0) : 0;
                String advice = root.has("advice") ? root.get("advice").asText("") : "";

                if (suggestReorderQuantity > 0 && suggestReorderQuantity % 10 != 0) {
                    suggestReorderQuantity = ((suggestReorderQuantity + 9) / 10) * 10;
                }

                AiReorderRecommendVO vo = new AiReorderRecommendVO();
                vo.setProductId(product.getId());
                vo.setProductName(productName);
                vo.setProductCode(productCode);
                vo.setCurrentStock(currentStock);
                vo.setMinStock(minStock);
                vo.setForecastSales7Days(forecastSales7Days);
                vo.setSuggestReorderQuantity(suggestReorderQuantity);
                vo.setAdvice(advice);

                return vo;

            } catch (Exception e) {
                log.warn("处理商品 {} 的AI补货建议时发生异常: {}", product.getName(), e.getMessage());
                return null;
            }
        });
    }

    /**
     * 生成推荐列表的具体实现：并行调用AI模型处理所有商品，显著提升性能
     */
    private List<AiReorderRecommendVO> generateRecommendations() {
        List<AiReorderRecommendVO> result = new ArrayList<>();

        List<Product> products = productMapper.selectAll();
        if (products == null || products.isEmpty()) {
            return result;
        }
        long startTime = System.currentTimeMillis();
        log.info("开始并行处理 {} 个商品的AI补货建议", products.size());

        // 测试专用：限制只处理前3个商品
        int testLimit = 3;
        products = products.subList(0, Math.min(testLimit, products.size()));

        // 创建所有商品的异步调用
        List<CompletableFuture<AiReorderRecommendVO>> futures = new ArrayList<>();
        for (Product product : products) {
            futures.add(processProductAsync(product));
        }

        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );
            allFutures.get();

            for (CompletableFuture<AiReorderRecommendVO> future : futures) {
                try {
                    AiReorderRecommendVO vo = future.get();
                    if (vo != null) {
                        result.add(vo);
                    }
                } catch (Exception e) {
                    log.warn("收集AI补货建议结果时发生异常: {}", e.getMessage());
                }
            }
            long endTime = System.currentTimeMillis();
            log.info("AI补货建议并行处理完成，共获得 {} 个有效建议，耗时: {} ms", result.size(), endTime - startTime);

        } catch (Exception e) {
            log.error("并行处理AI补货建议时发生异常: {}", e.getMessage());
            return new ArrayList<>();
        }

        return result;
    }
}
```

### 7.5 AI 商品详情生成服务

**文件路径**: `service/impl/AiProductServiceImpl.java`

```1:72:src/main/java/com/swiftstock/service/impl/AiProductServiceImpl.java
package com.swiftstock.service.impl;

import com.swiftstock.service.AiProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI 商品详情生成服务实现
 */
@Slf4j
@Service
public class AiProductServiceImpl implements AiProductService {

    private final ChatClient chatClient;

    @Autowired
    public AiProductServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 生成商品详情文案（调用大模型）
     */
    @Override
    public String generateProductDetail(String productName, String supplier, String category) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("productName 不能为空");
        }

        String supplierSafe = supplier == null ? "" : supplier;
        String categorySafe = category == null ? "" : category;

        String prompt = ""
                + "你是一名专业的电商商品详情文案编辑。请根据以下信息，为商品生成一段详细、吸引人的详情描述，适合放在商品详情页。\n\n"
                + "商品名称：" + productName + "\n"
                + "供应商：" + supplierSafe + "\n"
                + "分类：" + categorySafe + "\n\n"
                + "要求：\n"
                + "- 长度 100-200 字\n"
                + "- 内容包括：品牌介绍、材质特点、设计亮点、功能卖点、适用场景、穿着体验、洗护建议等\n"
                + "- 语言自然流畅、专业有营销感，使用"轻薄透气""速干科技""百搭经典""高端工艺"等词汇\n"
                + "- 适合运动服装鞋帽类商品\n"
                + "- 输出纯文本，不要任何 markdown、标题或额外说明\n\n"
                + "请直接输出商品详情文案：";

        try {
            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                return "AI生成失败，请手动填写";
            }

            return aiResponse.trim();
        } catch (Exception e) {
            log.error("调用 AI 生成商品详情失败", e);
            return "AI生成失败，请手动填写";
        }
    }
}
```

### 7.6 AI 补货推荐值对象

**文件路径**: `dto/AiReorderRecommendVO.java`

```1:21:src/main/java/com/swiftstock/dto/AiReorderRecommendVO.java
package com.swiftstock.dto;

import lombok.Data;

/**
 * AI 补货推荐VO
 */
@Data
public class AiReorderRecommendVO {
    private Long productId;
    private String productName;
    private String productCode;
    private Integer currentStock;
    private Integer minStock;
    private Integer forecastSales7Days;
    private Integer suggestReorderQuantity;
    private String advice;
}
```

---

## 八、公共组件

### 8.1 统一响应结果

**文件路径**: `dto/Result.java`

```java
package com.swiftstock.dto;

import lombok.Data;

/**
 * 统一返回结果封装
 *
 * <p>结构：{success: boolean, message?: string, data?: object}
 */
@Data
public class Result<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> ok(T data, String message) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setData(data);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
}
```

### 8.2 全局异常处理器

**文件路径**: `config/GlobalExceptionHandler.java`

```1:37:src/main/java/com/swiftstock/config/GlobalExceptionHandler.java
package com.swiftstock.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * <p>捕获未处理的异常并统一返回格式化的错误响应（包含时间戳和请求路径），同时记录错误日志，便于排查。</p>
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(HttpServletRequest request, Exception e) {
        log.error("Request: " + request.getRequestURL() + " raised " + e.getClass(), e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "服务器内部错误：" + e.getMessage());
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        response.put("path", request.getRequestURI());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

### 8.3 订单创建 DTO

**文件路径**: `dto/OrderCreateDTO.java`

```1:26:src/main/java/com/swiftstock/dto/OrderCreateDTO.java
package com.swiftstock.dto;

import lombok.Data;
import java.util.List;

/**
 * 订单创建DTO
 */
@Data
public class OrderCreateDTO {
    private String orderNo;
    private String customerName;
    private String customerPhone;
    private String status;
    private String remark;
    private List<OrderItemDTO> items;

    @Data
    public static class OrderItemDTO {
        private Long productId;
        private String productName;
        private Integer quantity;
        private Double price;
        private Double amount;
    }
}
```

---

## 附录

### A. 项目结构

```
src/main/java/com/swiftstock/
├── SwiftStockApplication.java    # 启动类
├── config/                       # 配置类
│   ├── CorsConfig.java
│   ├── GlobalExceptionHandler.java
│   ├── OpenApiConfig.java
│   ├── OrderStatusTypeHandler.java
│   └── SecurityConfig.java
├── controller/                   # 控制器层
│   ├── AiForecastController.java
│   ├── AiProductController.java
│   ├── AuthController.java
│   ├── CategoryController.java
│   ├── DashboardController.java
│   ├── InventoryController.java
│   ├── OrderController.java
│   ├── ProductController.java
│   ├── ReportController.java
│   ├── SalesController.java
│   ├── StockAlertController.java
│   ├── SupplierController.java
│   └── SupplyRecordController.java
├── dto/                          # 数据传输对象
│   ├── AiProductGenerateDTO.java
│   ├── AiReorderRecommendVO.java
│   ├── OrderCreateDTO.java
│   ├── Result.java
│   └── SupplyRecordDTO.java
├── entity/                       # 实体类
│   ├── Admin.java
│   ├── Category.java
│   ├── InventoryRecord.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── OrderStatus.java
│   ├── OrderStatusHistory.java
│   ├── Product.java
│   ├── Supplier.java
│   └── SupplyRecord.java
├── mapper/                       # MyBatis Mapper
│   ├── AdminMapper.java
│   ├── CategoryMapper.java
│   ├── InventoryRecordMapper.java
│   ├── OrderItemMapper.java
│   ├── OrderMapper.java
│   ├── OrderStatusHistoryMapper.java
│   ├── ProductMapper.java
│   ├── SupplierMapper.java
│   └── SupplyRecordMapper.java
├── security/                     # 安全模块
│   ├── AdminPrincipal.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtTokenUtil.java
└── service/                      # 业务层
    ├── impl/                     # 服务实现
    │   ├── AdminServiceImpl.java
    │   ├── AiForecastServiceImpl.java
    │   ├── AiProductServiceImpl.java
    │   ├── CategoryServiceImpl.java
    │   ├── InventoryServiceImpl.java
    │   ├── OrderServiceImpl.java
    │   ├── ProductServiceImpl.java
    │   ├── SalesServiceImpl.java
    │   ├── StockAlertServiceImpl.java
    │   ├── SupplierServiceImpl.java
    │   └── SupplyRecordServiceImpl.java
    ├── AdminService.java
    ├── AiForecastService.java
    ├── AiProductService.java
    ├── CategoryService.java
    ├── InventoryService.java
    ├── OrderService.java
    ├── ProductService.java
    ├── SalesService.java
    ├── StockAlertService.java
    ├── SupplierService.java
    └── SupplyRecordService.java
```

### B. 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.x | 应用框架 |
| Spring Security | 6.x | 安全框架 |
| MyBatis | 3.x | ORM 框架 |
| MySQL | 8.x | 数据库 |
| JWT (auth0) | - | Token 认证 |
| Spring AI | - | 大模型集成 |
| Lombok | - | 代码简化 |
| Slf4j/Logback | - | 日志框架 |

---

*文档生成时间: 2026-02-07*
*项目: SwiftStock 电商仓库管理系统*

