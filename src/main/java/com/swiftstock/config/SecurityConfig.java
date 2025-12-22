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
                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                // 认证相关接口允许匿名访问（登录/登出等）
                .requestMatchers("/auth/**").permitAll()
                // 暴露给前端的公共 API（如无需鉴权的接口）
                .requestMatchers("/api/**").permitAll()
                // 其它未明确允许的请求需要认证
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable()); // 使用 JWT 时通常禁用 CSRF（注意：若使用基于 Cookie 的认证请开启 CSRF）

        // 在用户名/密码认证过滤器之前插入 JWT 认证过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt 密码编码器 bean，用于密码哈希与比对
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 