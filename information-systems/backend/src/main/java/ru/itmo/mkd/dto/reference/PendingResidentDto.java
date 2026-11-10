package ru.itmo.mkd.dto.reference;

import ru.itmo.mkd.domain.OwnershipType;

import java.time.OffsetDateTime;

public record PendingResidentDto(Long userId, String fullName, String phone, Long apartmentId,
                                 String apartmentNumber, String address, OwnershipType ownership,
                                 OffsetDateTime linkedAt) {
}
