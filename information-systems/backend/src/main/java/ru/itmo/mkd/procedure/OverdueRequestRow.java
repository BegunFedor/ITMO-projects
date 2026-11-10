package ru.itmo.mkd.procedure;

import ru.itmo.mkd.domain.RequestStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OverdueRequestRow(Long requestId, Long companyId, String company, String address, String category,
                                RequestStatus status, OffsetDateTime createdAt, OffsetDateTime deadlineAt,
                                BigDecimal hoursOverdue, boolean hasComplaint) {
}
