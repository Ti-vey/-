package com.warehouse.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：负责令牌的生成与解析。
 */
@Component
public class JwtUtil {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_REAL_NAME = "realName";
    private static final String CLAIM_ROLE = "role";

    @Value("${warehouse.jwt.secret}")
    private String secret;

    @Value("${warehouse.jwt.expire-hours}")
    private long expireHours;

    private SecretKey signKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成登录令牌。
     */
    public String createToken(Long userId, String username, String realName, String roleCode) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expireHours * 60 * 60 * 1000);
        return Jwts.builder()
                .setSubject(username)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_REAL_NAME, realName)
                .claim(CLAIM_ROLE, roleCode)
                .setIssuedAt(now)
                .setExpiration(expire)
                .signWith(signKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析令牌，令牌非法或过期时抛出 JwtException。
     */
    public LoginUser parseToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        LoginUser user = new LoginUser();
        Object userId = claims.get(CLAIM_USER_ID);
        user.setUserId(userId == null ? null : Long.valueOf(userId.toString()));
        user.setUsername(claims.getSubject());
        user.setRealName((String) claims.get(CLAIM_REAL_NAME));
        user.setRoleCode((String) claims.get(CLAIM_ROLE));
        return user;
    }
}
