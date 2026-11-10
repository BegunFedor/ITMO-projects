package ru.itmo.mkd.dto.request;

public record ExecutorOptionDto(Long id, String fullName, boolean matchesSpecialization, long activeRequests) {
}
