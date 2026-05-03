package com.example.devlogqna.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessValidityMs;
    private final long refreshValidityMs;
    private final StringRedisTemplate redisTemplate;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity}") long accessValidityMs,
            @Value("${jwt.refresh-token-validity}") long refreshValidityMs,
            StringRedisTemplate redisTemplate) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessValidityMs = accessValidityMs;
        this.refreshValidityMs = refreshValidityMs;
        this.redisTemplate = redisTemplate;
    }

    // Access Token 생성
    public String generateAccessToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessValidityMs);
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // Access Token 은 JTI를 포함하지 않으므로, 로그아웃(블랙리스트)은 jti 추출 시 직접 생성한 UUID 활용
    // Refresh Token 생성 (Redis 저장용)
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshValidityMs);
        String refreshToken = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
        // Redis에 저장 (key: refresh:<username>, value: token, TTL: 7일)
        redisTemplate.opsForValue().set(
                "refresh:" + username,
                refreshToken,
                refreshValidityMs,
                TimeUnit.MILLISECONDS
        );
        return refreshToken;
    }

    // Refresh Token 검증 + Redis 대조
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = parseClaims(refreshToken);
            String username = claims.getSubject();
            String storedToken = redisTemplate.opsForValue().get("refresh:" + username);
            return refreshToken.equals(storedToken) && !claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return false;
        }
    }

    // Refresh Token 무효화 (로그아웃 시)
    public void invalidateRefreshToken(String username) {
        redisTemplate.delete("refresh:" + username);
    }

    // Access Token Blacklist 등록 (로그아웃 시)
    public void blacklistAccessToken(String accessToken, long remainingMillis) {
        redisTemplate.opsForValue().set(
                "blacklist:" + accessToken,
                "logout",
                remainingMillis,
                TimeUnit.MILLISECONDS
        );
    }

    // Access Token Blacklist 확인
    public boolean isAccessTokenBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + accessToken));
    }

    // JWT 파싱
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Access Token 유효성 검증 (블랙리스트 포함)
    public boolean validateAccessToken(String token) {
        try {
            parseClaims(token);
            return !isAccessTokenBlacklisted(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰 만료까지 남은 시간 (ms)
    public long getRemainingMillis(String token) {
        return parseClaims(token).getExpiration().getTime() - System.currentTimeMillis();
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String getRole(String token) {
        return (String) parseClaims(token).get("role");
    }
}
