package com.omnistore.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.omnistore.entity.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String SECRET = "very_secret_key_change_later";

    private static SecretKey getSigningKey() {
        try {
            byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(SECRET.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(sha256);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to create signing key", e);
        }
    }

    public String generateToken(User user) {
        SecretKey key = getSigningKey();
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
