package com.omnistore.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

    private static final String SECRET_KEY = "your-secret-key"; // Strong secret key

    private static SecretKey getSigningKey() {
        try {
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(sha256);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to create signing key", e);
        }
    }

    public static String generateToken(String email, String role) {
        SecretKey key = getSigningKey();
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role) // Add user role as claim
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 hour expiration
                .signWith(key, SignatureAlgorithm.HS256) // Sign with HS256 algorithm
                .compact();
    }

    public static Claims parseToken(String token) {
        SecretKey key = getSigningKey();
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
