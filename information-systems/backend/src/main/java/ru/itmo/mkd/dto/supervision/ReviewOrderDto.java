package ru.itmo.mkd.dto.supervision;

import jakarta.validation.constraints.*;

public record ReviewOrderDto(@NotNull Boolean accepted, @Size(max = 1000) String comment) {
}
