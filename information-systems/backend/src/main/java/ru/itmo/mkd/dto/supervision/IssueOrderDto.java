package ru.itmo.mkd.dto.supervision;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public record IssueOrderDto(@NotNull Long companyId,
                            @NotBlank @Size(max = 2000) String violation,
                            @NotNull @Future LocalDate dueDate,
                            List<@NotNull Long> requestIds,
                            List<@NotNull Long> complaintIds) {
}
