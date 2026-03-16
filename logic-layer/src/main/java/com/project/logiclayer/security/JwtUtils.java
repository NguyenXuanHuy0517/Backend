package com.project.logiclayer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

/**
 * JwtUtils (cập nhật): Bổ sung các method đọc và xác thực token,
 * cần thiết cho JwtAuthFilter.
 *
 * THAY ĐỔI SO VỚI FILE GỐC:
 * - SECRET_KEY không còn hardcode — đọc từ application.properties
 * - Thêm extractPhoneNumber() để JwtAuthFilter lấy thông tin user
 * - Thêm validateToken() để kiểm tra token có hợp lệ không
 * - Thêm extractClaim() làm utility method dùng chung
 *
 * Cần thêm vào application.properties:
 *   jwt.secret=your-very-long-secret-key-at-least-256-bits
 *   jwt.expiration=86400000
 */
@Component
public class JwtUtils {

    // Đọc từ application.properties thay vì hardcode
    @Value("${jwt.secret:defaultSecretKeyForDevOnly_ChangeInProduction_MustBe256Bits}")
    private String secretKeyString;

    @Value("${jwt.expiration:86400000}")
    private long expirationTime;

    // Lấy Key từ chuỗi secret (lazy init để đọc được @Value)
    private Key getSigningKey() {
        byte[] keyBytes = secretKeyString.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Tạo JWT token từ số điện thoại và vai trò người dùng.
     * Được gọi trong AuthService khi đăng nhập thành công.
     */
    public String generateToken(String phoneNumber, String role) {
        return Jwts.builder()
                .setSubject(phoneNumber)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Lấy số điện thoại (subject) từ token.
     * Được gọi trong JwtAuthFilter để biết user nào đang gửi request.
     */
    public String extractPhoneNumber(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Lấy role từ token.
     * Dùng khi cần kiểm tra quyền mà không cần truy vấn DB.
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * Kiểm tra token có hợp lệ không:
     * - Chữ ký đúng
     * - Chưa hết hạn
     * - Subject khớp với username truyền vào
     */
    public boolean validateToken(String token, String phoneNumber) {
        try {
            String extractedPhone = extractPhoneNumber(token);
            return extractedPhone.equals(phoneNumber) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // --- Private helper methods ---

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Utility method chung để extract bất kỳ claim nào từ token.
     * Xử lý việc parse và xác thực chữ ký JWT.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
}