package ru.itmo.mkd.dto.request;

import jakarta.validation.constraints.*;

public record AssignDto(@NotNull Long executorId, @Size(max = 1000) String comment) {
}
