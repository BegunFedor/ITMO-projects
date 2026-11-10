package ru.itmo.mkd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.dto.reference.ApartmentDto;
import ru.itmo.mkd.dto.reference.CategoryDto;
import ru.itmo.mkd.dto.reference.HouseDto;
import ru.itmo.mkd.dto.reference.MyApartmentDto;
import ru.itmo.mkd.mapper.ReferenceMapper;
import ru.itmo.mkd.repository.ApartmentRepository;
import ru.itmo.mkd.repository.HouseRepository;
import ru.itmo.mkd.repository.RequestCategoryRepository;
import ru.itmo.mkd.repository.ResidentApartmentRepository;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.service.access.StaffScope;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReferenceService {

    private final RequestCategoryRepository categoryRepository;
    private final HouseRepository houseRepository;
    private final ApartmentRepository apartmentRepository;
    private final ResidentApartmentRepository residentApartmentRepository;
    private final StaffScope staffScope;
    private final ReferenceMapper mapper;

    public ReferenceService(RequestCategoryRepository categoryRepository, HouseRepository houseRepository,
                            ApartmentRepository apartmentRepository,
                            ResidentApartmentRepository residentApartmentRepository,
                            StaffScope staffScope, ReferenceMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.houseRepository = houseRepository;
        this.apartmentRepository = apartmentRepository;
        this.residentApartmentRepository = residentApartmentRepository;
        this.staffScope = staffScope;
        this.mapper = mapper;
    }

    public List<CategoryDto> categories() {
        return mapper.toCategoryDtos(categoryRepository.findByActiveTrueOrderByName());
    }

    public List<HouseDto> houses() {
        return mapper.toHouseDtos(houseRepository.findAllByOrderByAddress());
    }

    public List<ApartmentDto> apartments(Long houseId) {
        return mapper.toApartmentDtos(apartmentRepository.findByHouseIdOrderById(houseId));
    }

    public List<MyApartmentDto> myApartments(CurrentUser resident) {
        return mapper.toMyApartments(residentApartmentRepository.findByResident(resident.id()));
    }

    public List<HouseDto> companyHouses(CurrentUser admin) {
        return mapper.toHouseDtos(houseRepository.findByCompanyIdOrderByAddress(staffScope.companyOf(admin)));
    }
}
