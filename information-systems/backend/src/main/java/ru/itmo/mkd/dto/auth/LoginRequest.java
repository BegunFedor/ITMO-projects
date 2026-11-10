package ru.itmo.mkd.dto.auth;

import jakarta.validation.constraints.*;

public record LoginRequest(@NotBlank String login, @NotBlank String password) {
}
