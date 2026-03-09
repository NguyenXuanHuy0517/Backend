package com.project.logiclayer.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JwtUtils: Lớp tiện ích tạo và xác thực Token.
 */
@Component
public class JwtUtils {

    // Secret Key (Trong thực tế nên để ở application.properties)
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_TIME = 86400000; // 24 giờ

    /**
     * Tạo Token dựa trên Số điện thoại và Vai trò của User.
     */
    public String generateToken(String phoneNumber, String role) {
        return Jwts.builder()
                .setSubject(phoneNumber)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }
}