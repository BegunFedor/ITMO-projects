package ru.itmo.mkd.dto.supervision;

import jakarta.validation.constraints.*;

public record FileComplaintDto(@NotNull Long requestId, @NotBlank @Size(min = 10, max = 2000) String body) {
}
