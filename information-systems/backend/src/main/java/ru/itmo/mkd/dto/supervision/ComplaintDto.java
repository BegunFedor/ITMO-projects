package ru.itmo.mkd.dto.supervision;

import ru.itmo.mkd.domain.ComplaintStatus;

import java.time.OffsetDateTime;

public record ComplaintDto(Long id, Long requestId, ComplaintStatus status, String body, String residentName,
                           String address, String categoryName, Long companyId, String companyName,
                           OffsetDateTime createdAt, OffsetDateTime decidedAt, String decisionComment, Long orderId) {
}
