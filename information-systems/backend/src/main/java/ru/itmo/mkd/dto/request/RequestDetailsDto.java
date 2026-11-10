package ru.itmo.mkd.dto.request;

import ru.itmo.mkd.domain.RequestStatus;

import java.time.OffsetDateTime;

public record RequestDetailsDto(Long id, RequestStatus status, Long categoryId, String categoryName,
                                Long houseId, String address, String companyName,
                                Long apartmentId, String apartmentNumber,
                                String description, String authorName, String authorPhone,
                                Long executorId, String executorName, String resultText,
                                Long previousRequestId, OffsetDateTime createdAt, OffsetDateTime deadlineAt,
                                OffsetDateTime statusChangedAt, boolean overdue) {
}
