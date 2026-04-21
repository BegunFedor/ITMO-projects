package org.example.dao;

import org.example.entity.RefreshToken;
import org.example.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepositoryInterface extends Repository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserAndRevokedFalse(User user);

    void deleteByExpiryDateBefore(LocalDateTime date);

    boolean existsByTokenAndRevokedFalse(String token);

    void deleteExpiredTokens(LocalDateTime currentDate);
}
