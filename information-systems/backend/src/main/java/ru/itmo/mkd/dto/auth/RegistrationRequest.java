package ru.itmo.mkd.dto.auth;

import jakarta.validation.constraints.*;
import ru.itmo.mkd.domain.OwnershipType;

public record RegistrationRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_.-]{3,50}$") String login,
        @NotBlank @Size(min = 6, max = 100) String password,
        @NotBlank @Size(max = 150) String fullName,
        @Pattern(regexp = "^\\+?[0-9() -]{5,20}$") String phone,
        @Email @Size(max = 100) String email,
        @NotNull Long apartmentId,
        @NotNull OwnershipType ownership) {
}
