package ru.itmo.mkd.dto.reference;

import ru.itmo.mkd.domain.OwnershipType;

public record MyApartmentDto(Long apartmentId, String number, Long houseId, String address,
                             OwnershipType ownership, boolean confirmed) {
}
