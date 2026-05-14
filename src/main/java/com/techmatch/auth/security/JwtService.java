package com.techmatch.auth.security;

import com.techmatch.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtService {

    private static final String USER_ID_CLAIM = "userId";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = buildSecretKey(jwtProperties.getSecret());
    }

    public String generateToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expireAt = now.plusSeconds(jwtProperties.getExpireSeconds());
        return Jwts.builder()
                .subject(username)
                .claim(USER_ID_CLAIM, userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expireAt))
                .signWith(secretKey)
                .compact();
    }

    public LoginUserPrincipal parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Object userIdValue = claims.get(USER_ID_CLAIM);
        if (!(userIdValue instanceof Number userIdNumber) || claims.getSubject() == null) {
            throw new SecurityException("token missing required claims");
        }
        return new LoginUserPrincipal(userIdNumber.longValue(), claims.getSubject());
    }

    public Long getExpireSeconds() {
        return jwtProperties.getExpireSeconds();
    }

    private SecretKey buildSecretKey(String rawSecret) {
        try {
            byte[] keyBytes;
            try {
                keyBytes = Decoders.BASE64.decode(rawSecret);
            } catch (IllegalArgumentException | DecodingException exception) {
                keyBytes = MessageDigest.getInstance("SHA-256")
                        .digest(rawSecret.getBytes(StandardCharsets.UTF_8));
            }
            return new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (Exception exception) {
            throw new IllegalStateException("failed to initialize jwt secret", exception);
        }
    }
}
