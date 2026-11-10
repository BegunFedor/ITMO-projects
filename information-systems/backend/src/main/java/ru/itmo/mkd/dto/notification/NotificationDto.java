package ru.itmo.mkd.dto.notification;

import ru.itmo.mkd.domain.NotificationEvent;
import ru.itmo.mkd.domain.NotificationTarget;
import ru.itmo.mkd.domain.RequestStatus;

import java.time.OffsetDateTime;

public record NotificationDto(Long id, NotificationEvent event, NotificationTarget entityType, Long entityId,
                              RequestStatus status, boolean read, OffsetDateTime createdAt) {
}
