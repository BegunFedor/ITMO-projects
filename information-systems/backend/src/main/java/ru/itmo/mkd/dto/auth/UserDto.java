package ru.itmo.mkd.dto.auth;

import ru.itmo.mkd.domain.UserRole;

public record UserDto(Long id, String login, String fullName, UserRole role) {
}
