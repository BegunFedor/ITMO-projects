package org.example.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import org.example.dao.RefreshTokenRepository;
import org.example.entity.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Stateless
public class JWTService {

    @EJB
    private RefreshTokenRepository refreshTokenRepository;

    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "57b0f8aa1361081fd86851ee4594bc475d00d095efd25b3113157f8ecddd900c".getBytes()
    );

    private final long ACCESS_EXPIRATION = 15 * 60 * 1000;

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getName())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .setId(UUID.randomUUID().toString())
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }

            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }


    public String extractJti(String token) {
        return extractAllClaims(token).getId();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}