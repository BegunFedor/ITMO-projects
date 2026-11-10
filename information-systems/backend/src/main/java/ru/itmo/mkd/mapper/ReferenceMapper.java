package ru.itmo.mkd.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.mkd.dto.reference.ApartmentDto;
import ru.itmo.mkd.dto.reference.CategoryDto;
import ru.itmo.mkd.dto.reference.HouseDto;
import ru.itmo.mkd.dto.reference.MyApartmentDto;
import ru.itmo.mkd.dto.reference.PendingResidentDto;
import ru.itmo.mkd.entity.Apartment;
import ru.itmo.mkd.entity.House;
import ru.itmo.mkd.entity.RequestCategory;
import ru.itmo.mkd.entity.ResidentApartment;

import java.util.List;

@Mapper
public interface ReferenceMapper {

    CategoryDto toDto(RequestCategory category);

    List<CategoryDto> toCategoryDtos(List<RequestCategory> categories);

    HouseDto toDto(House house);

    List<HouseDto> toHouseDtos(List<House> houses);

    ApartmentDto toDto(Apartment apartment);

    List<ApartmentDto> toApartmentDtos(List<Apartment> apartments);

    @Mapping(target = "apartmentId", source = "apartment.id")
    @Mapping(target = "number", source = "apartment.number")
    @Mapping(target = "houseId", source = "apartment.house.id")
    @Mapping(target = "address", source = "apartment.house.address")
    MyApartmentDto toMyApartment(ResidentApartment link);

    List<MyApartmentDto> toMyApartments(List<ResidentApartment> links);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "phone", source = "user.phone")
    @Mapping(target = "apartmentId", source = "apartment.id")
    @Mapping(target = "apartmentNumber", source = "apartment.number")
    @Mapping(target = "address", source = "apartment.house.address")
    PendingResidentDto toPending(ResidentApartment link);

    List<PendingResidentDto> toPendingList(List<ResidentApartment> links);
}
