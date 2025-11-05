package com.swiftstock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**", 
                            "/swiftstock/static/**", "/swiftstock/css/**", "/swiftstock/js/**", 
                            "/swiftstock/images/**", "/swiftstock/webjars/**", 
                            "/error", "/test/**",
                            "/inventory/record/**",  // 允许访问库存记录
                            // API Documentation paths - 完全公开
                            "/swagger-ui.html", "/swagger-ui/**", 
                            "/v3/api-docs/**", "/api-docs/**",
                            "/v3/api-docs.yaml", "/api-docs.yaml",
                            // API 接口路径 - 完全公开
                            "/products/**", "/orders/**", "/inventory/**", 
                            "/reports/**", "/categories/**", "/auth/**", 
                            "/stock-alert/**", "/migration/**", "/sales/**",
                            "/dashboard/**").permitAll()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable());  // 开发阶段暂时禁用CSRF
        
        return http.build();
    }
} 