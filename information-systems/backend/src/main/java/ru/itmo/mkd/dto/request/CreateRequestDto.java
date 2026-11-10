package ru.itmo.mkd.dto.request;

import jakarta.validation.constraints.*;

public record CreateRequestDto(@NotNull Long houseId, Long apartmentId, @NotNull Long categoryId,
                               @NotBlank @Size(min = 10, max = 2000) String description,
                               Long previousRequestId) {
}
