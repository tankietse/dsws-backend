package com.webgis.dsws.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Lớp tiện ích xử lý JWT Token.
 * Cung cấp các phương thức để tạo, xác thực và trích xuất thông tin từ JWT
 * token.
 */
@Component
public class JwtUtil {

    @Value("${jwt.token-expiration}")
    private long tokenExpiration;

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    /**
     * Lấy khóa ký để mã hóa và giải mã token
     * 
     * @return Key đối tượng khóa để ký JWT
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    /**
     * Tạo JWT token cho người dùng
     * 
     * @param username Tên đăng nhập của người dùng
     * @return JWT token dạng chuỗi
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Kiểm tra tính hợp lệ của token
     * 
     * @param token Token cần kiểm tra
     * @return true nếu token hợp lệ, false nếu ngược lại
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Trích xuất tên người dùng từ token
     * 
     * @param token Token JWT cần trích xuất thông tin
     * @return Tên người dùng
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public long getExpirationInSeconds() {

        return tokenExpiration;

    }

}