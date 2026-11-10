package ru.itmo.mkd.dto.supervision;

import jakarta.validation.constraints.*;

public record OrderReportDto(@NotBlank @Size(max = 5000) String report) {
}
