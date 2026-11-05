package com.furniture.config;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret must be configured");
        }
        if (expiration <= 0) {
            throw new IllegalStateException("jwt.expiration must be greater than zero");
        }

        byte[] keyBytes;
        try {
            keyBytes = java.util.Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("jwt.secret must be valid Base64", ex);
        }

        try {
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "jwt.secret must decode to a sufficiently strong HMAC key", ex);
        }

        this.expiration = expiration;
    }

    public String generateToken(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required to generate a JWT");
        }

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean validateToken(String token, String email) {
        if (token == null || token.isBlank() || email == null || email.isBlank()) {
            return false;
        }

        try {
            Claims claims = extractClaims(token);
            String subject = claims.getSubject();
            Date expiresAt = claims.getExpiration();
            Date issuedAt = claims.getIssuedAt();
            Date now = new Date();

            return subject != null
                    && subject.equals(email)
                    && expiresAt != null
                    && expiresAt.after(now)
                    && (issuedAt == null || !issuedAt.after(now));
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Claims extractClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("JWT is required");
        }

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
