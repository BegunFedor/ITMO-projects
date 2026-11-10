package ru.itmo.mkd.dto.request;

import ru.itmo.mkd.domain.RequestStatus;
import ru.itmo.mkd.domain.UserRole;

import java.time.OffsetDateTime;

public record StatusHistoryDto(RequestStatus status, String changedByName,
                               UserRole changedByRole, OffsetDateTime changedAt, String comment) {
}
