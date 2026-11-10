package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.mkd.entity.AppUser;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByLoginIgnoreCase(String login);
}
