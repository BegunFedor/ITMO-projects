package org.example.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import org.example.entity.User;

import java.util.List;
import java.util.Optional;

@Stateless
@LocalBean
public class UserRepository extends BaseRepository<User, Long> implements UserRepositoryInterface {

    public UserRepository() {
        super(User.class);
    }

    @Override
    public Optional<User> findByName(String name) {
        return entityManager.createQuery("SELECT u FROM User u WHERE u.name = ?1", User.class)
                .setParameter(1, name)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<User> findActiveUsers() {
        return entityManager.createQuery("SELECT u FROM User u", User.class)
                .getResultList();
    }


    @Override
    public Optional<User> findByEmail(String email) {
        return entityManager.createQuery(
                        "SELECT u FROM User u WHERE u.name = :email", User.class)
                .setParameter("email", email)
                .getResultStream()
                .findFirst();
    }

    @Override
    public Boolean isVerified(String email) {
        return findByEmail(email)
                .map(User::getVerified)
                .orElse(false);
    }

    @Override
    public Boolean isVerifiedById(Long userId) {
        return findById(userId)
                .map(User::getVerified)
                .orElse(false);
    }
}