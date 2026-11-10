package ru.itmo.mkd.dto.request;

import ru.itmo.mkd.domain.UserRole;

import java.time.OffsetDateTime;

public record CommentDto(Long id, String authorName, UserRole authorRole, String body, OffsetDateTime createdAt) {
}
