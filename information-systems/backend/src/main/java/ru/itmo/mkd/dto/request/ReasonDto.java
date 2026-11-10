package ru.itmo.mkd.dto.request;

import jakarta.validation.constraints.*;

public record ReasonDto(@NotBlank @Size(max = 1000) String reason) {
}
