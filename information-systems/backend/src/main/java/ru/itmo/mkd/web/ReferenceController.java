package ru.itmo.mkd.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.mkd.domain.UserRole;
import ru.itmo.mkd.dto.reference.ApartmentDto;
import ru.itmo.mkd.dto.reference.CategoryDto;
import ru.itmo.mkd.dto.reference.HouseDto;
import ru.itmo.mkd.dto.reference.MyApartmentDto;
import ru.itmo.mkd.security.Authenticated;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.security.RequiresRole;
import ru.itmo.mkd.service.ReferenceService;

import java.util.List;

@RestController
@RequestMapping("/api/reference")
public class ReferenceController {

    private final ReferenceService referenceService;

    public ReferenceController(ReferenceService referenceService) {
        this.referenceService = referenceService;
    }

    @GetMapping("/houses")
    public List<HouseDto> houses() {
        return referenceService.houses();
    }

    @GetMapping("/houses/{houseId}/apartments")
    public List<ApartmentDto> apartments(@PathVariable Long houseId) {
        return referenceService.apartments(houseId);
    }

    @GetMapping("/categories")
    @Authenticated
    public List<CategoryDto> categories() {
        return referenceService.categories();
    }

    @GetMapping("/my-apartments")
    @RequiresRole(UserRole.RESIDENT)
    public List<MyApartmentDto> myApartments(CurrentUser user) {
        return referenceService.myApartments(user);
    }

    @GetMapping("/company-houses")
    @RequiresRole(UserRole.COMPANY_ADMIN)
    public List<HouseDto> companyHouses(CurrentUser user) {
        return referenceService.companyHouses(user);
    }
}
