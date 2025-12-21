package com.swiftstock.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.swiftstock.entity.Admin;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 简单的 JWT 工具类（基于 HMAC SHA256）
 * <p>注意：密钥用在应用配置中更为安全；此处为演示直接内置。</p>
 */
@Component
public class JwtTokenUtil {

    // TODO: move to application.yml / env var for production
    private final String secret = "SwiftStockSecretKeyChangeMe";
    private final long expirationMillis = 1000L * 60 * 60 * 24; // 24h

    private final Algorithm algorithm = Algorithm.HMAC256(secret);
    private final JWTVerifier verifier = JWT.require(algorithm).build();

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


