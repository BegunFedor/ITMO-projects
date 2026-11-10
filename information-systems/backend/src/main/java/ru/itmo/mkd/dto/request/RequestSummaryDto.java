package ru.itmo.mkd.dto.request;

import ru.itmo.mkd.domain.RequestStatus;

import java.time.OffsetDateTime;

public record RequestSummaryDto(Long id, RequestStatus status, String categoryName, Long houseId, String address,
                                String apartmentNumber, String description, String executorName,
                                OffsetDateTime createdAt, OffsetDateTime deadlineAt, boolean overdue) {
}
