package org.example.dao;

import org.example.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryInterface extends Repository<User, Long>{
    Optional<User> findByName(String name);
    List<User> findActiveUsers();
    Optional<User> findByEmail(String email);
    Boolean isVerified(String email);
    Boolean isVerifiedById(Long userId);
}
