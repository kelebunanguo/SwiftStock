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
 * 从 Authorization: Bearer &lt;token&gt; 中解析 JWT，并把认证信息设置到 SecurityContext。
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
                        // 简单构造认证对象，角色/权限可按需扩展
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


