package org.example.domain.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.example.domain.model.Role;
import org.example.domain.model.User;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import io.jsonwebtoken.Jwts;

public class JwtProvider {

    private final SecretKey accessTokenKey;
    private final SecretKey refreshTokenKey;
    private final Duration accessTokenLife;
    private final Duration refreshTokenLife;

    public JwtProvider(
            String accessSecret,
            String refreshSecret,
            long accessTokenLife,
            long refreshTokenLife
    ) {
        this.accessTokenKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenLife = Duration.ofMinutes(accessTokenLife);
        this.refreshTokenLife = Duration.ofDays(refreshTokenLife);
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getUuid().toString())
                .claim("uuid", user.getUuid().toString())
                .claim("roles", user.getRoles().stream().map(Role::name).toList())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(accessTokenLife)))
                .signWith(accessTokenKey)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getUuid().toString())
                .claim("uuid", user.getUuid().toString())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(refreshTokenLife)))
                .signWith(refreshTokenKey)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, accessTokenKey);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, refreshTokenKey);
    }

    public Claims getAccessClaims(String token) {
        return getClaims(token, accessTokenKey);
    }

    public Claims getRefreshClaims(String token) {
        return getClaims(token, refreshTokenKey);
    }

    private boolean validateToken(String token, SecretKey key) {
        try {
            getClaims(token, key);
            return true;
        }
        catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims getClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
