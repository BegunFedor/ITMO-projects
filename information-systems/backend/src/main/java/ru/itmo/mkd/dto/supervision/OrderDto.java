package ru.itmo.mkd.dto.supervision;

import ru.itmo.mkd.domain.OrderStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderDto(Long id, String number, Long companyId, String companyName, String violation,
                       OffsetDateTime issuedAt, LocalDate dueDate, OrderStatus status, String reportText,
                       OffsetDateTime reportAt, OffsetDateTime reviewedAt, String reviewComment,
                       List<Long> requestIds) {
}
