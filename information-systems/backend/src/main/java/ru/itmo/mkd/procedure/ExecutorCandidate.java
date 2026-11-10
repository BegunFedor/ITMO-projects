package ru.itmo.mkd.procedure;

public record ExecutorCandidate(Long executorId, String fullName, boolean matchesSpecialization, long activeRequests) {
}
