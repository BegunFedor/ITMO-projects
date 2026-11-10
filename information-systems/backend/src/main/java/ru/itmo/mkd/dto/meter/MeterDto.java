package ru.itmo.mkd.dto.meter;

import ru.itmo.mkd.domain.ResourceType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MeterDto(Long id, ResourceType resource, String serialNumber, String address, String apartmentNumber,
                       BigDecimal lastValue, LocalDate lastDate) {
}
