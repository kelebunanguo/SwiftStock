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
                // static and swagger
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                // authentication endpoints
                .requestMatchers("/auth/**").permitAll()
                // allow all API endpoints (public)
                .requestMatchers("/api/**").permitAll()
                // other public endpoints you intentionally want public can be added here
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable()); // 使用 JWT 时通常禁用 CSRF（注意：若使用 Cookie-based auth 请开启）

        // Insert JWT filter before username/password auth filter
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