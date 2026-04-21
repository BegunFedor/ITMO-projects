package org.example.service;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import org.example.dao.RefreshTokenRepository;
import org.example.entity.RefreshToken;
import org.example.entity.User;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Stateless
public class RefreshTokenService {

    @EJB
    private RefreshTokenRepository refreshTokenRepository;

    private final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(30);

    public RefreshToken createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plus(REFRESH_TOKEN_DURATION);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(expiryDate)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }


    @Schedule(hour = "3", persistent = false)
    public void cleanupExpiredTokens() {
        try {
            refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("Token cleanup failed: " + e.getMessage());
        }
    }

}