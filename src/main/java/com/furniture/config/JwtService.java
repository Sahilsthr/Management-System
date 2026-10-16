package com.furniture.config;

import java.util.Date;

import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration}") long expiration){

        byte[] keyBytes = java.util.Base64.getDecoder().decode(secret);
        
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }
    public String generateToken(String email){
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                        .subject(email)
                        .issuedAt(now)
                        .expiration(expiry)
                        .signWith(secretKey)
                        .compact();

    }
    public String extractEmail(String token){
        return extractClaims(token).getSubject();
    }

    public boolean validateToken(String token, String email){
        Claims claims = extractClaims(token);

        return claims.getSubject().equals(email) && claims.getExpiration().after(new Date());
    }

    private Claims extractClaims(String token){
        return Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                        
    }
    
}
