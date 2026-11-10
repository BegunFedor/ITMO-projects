package ru.itmo.mkd.security;

import ru.itmo.mkd.domain.UserRole;

import java.io.Serializable;

public record CurrentUser(Long id, UserRole role) implements Serializable {
}
