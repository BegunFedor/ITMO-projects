package ru.itmo.mkd.dto.request;

import jakarta.validation.constraints.*;

public record NewCommentDto(@NotBlank @Size(max = 2000) String body) {
}
