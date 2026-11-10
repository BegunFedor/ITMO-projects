package ru.itmo.mkd.dto.reference;

import jakarta.validation.constraints.*;

public record ConfirmResidentRequest(@NotNull Long userId, @NotNull Long apartmentId) {
}
