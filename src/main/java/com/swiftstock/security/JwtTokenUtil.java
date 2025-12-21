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
 * <p>密钥与过期时间从配置中读取，生产环境请通过环境变量或配置中心管理。</p>
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


