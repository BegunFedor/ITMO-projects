package ru.itmo.mkd.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.mkd.dto.meter.MeterDto;
import ru.itmo.mkd.entity.Meter;
import ru.itmo.mkd.entity.MeterReading;

@Mapper
public interface MeterMapper {

    @Mapping(target = "id", source = "meter.id")
    @Mapping(target = "resource", source = "meter.resource")
    @Mapping(target = "serialNumber", source = "meter.serialNumber")
    @Mapping(target = "address", source = "meter.apartment.house.address")
    @Mapping(target = "apartmentNumber", source = "meter.apartment.number")
    @Mapping(target = "lastValue", source = "latest.value")
    @Mapping(target = "lastDate", source = "latest.readingDate")
    MeterDto toDto(Meter meter, MeterReading latest);
}
