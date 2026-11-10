package ru.itmo.mkd.dto.meter;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ReadingDto(@NotNull @DecimalMin("0") @Digits(integer = 9, fraction = 3) BigDecimal value) {
}
