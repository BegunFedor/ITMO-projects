package ru.itmo.mkd.dto.supervision;

import ru.itmo.mkd.domain.RequestStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OverdueRequestDto(Long requestId, Long companyId, String companyName, String address, String categoryName,
                                RequestStatus status, OffsetDateTime createdAt, OffsetDateTime deadlineAt,
                                BigDecimal hoursOverdue, boolean hasComplaint) {
}
