package org.example.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import org.example.entity.RefreshToken;
import org.example.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Stateless
@LocalBean
public class RefreshTokenRepository extends BaseRepository<RefreshToken, Long>
        implements RefreshTokenRepositoryInterface {

    public RefreshTokenRepository() {
        super(RefreshToken.class);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return entityManager.createQuery(
                        "SELECT rt FROM RefreshToken rt WHERE rt.token = ?1", RefreshToken.class)
                .setParameter(1, token)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<RefreshToken> findByUserAndRevokedFalse(User user) {
        return entityManager.createQuery(
                        "SELECT rt FROM RefreshToken rt WHERE rt.user = ?1 AND rt.revoked = false",
                        RefreshToken.class)
                .setParameter(1, user)
                .getResultList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deleteExpiredTokens(LocalDateTime currentDate) {
        int deletedCount = entityManager.createQuery(
                        "DELETE FROM RefreshToken rt WHERE rt.expiryDate < ?1")
                .setParameter(1, currentDate)
                .executeUpdate();
    }

    @Override
    public boolean existsByTokenAndRevokedFalse(String token) {
        return entityManager.createQuery(
                        "SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.token = ?1 AND rt.revoked = false",
                        Long.class)
                .setParameter(1, token)
                .getSingleResult() > 0;
    }

    @Override
    public void deleteByExpiryDateBefore(LocalDateTime date) {
        entityManager.createQuery(
                        "DELETE FROM RefreshToken rt WHERE rt.expiryDate < ?1")
                .setParameter(1, date)
                .executeUpdate();
    }
}