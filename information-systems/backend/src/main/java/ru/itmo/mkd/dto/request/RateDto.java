package ru.itmo.mkd.dto.request;

import jakarta.validation.constraints.*;

public record RateDto(@NotNull @Min(1) @Max(5) Short score, @Size(max = 1000) String review) {
}
