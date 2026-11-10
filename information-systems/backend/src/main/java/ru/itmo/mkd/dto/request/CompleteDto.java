package ru.itmo.mkd.dto.request;

import jakarta.validation.constraints.*;

public record CompleteDto(@NotBlank @Size(max = 2000) String result) {
}
