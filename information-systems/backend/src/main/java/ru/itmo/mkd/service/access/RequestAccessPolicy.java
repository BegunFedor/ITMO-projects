package ru.itmo.mkd.service.access;

import org.springframework.stereotype.Component;
import ru.itmo.mkd.entity.Request;
import ru.itmo.mkd.exception.ForbiddenException;
import ru.itmo.mkd.repository.ResidentApartmentRepository;
import ru.itmo.mkd.security.CurrentUser;

@Component
public class RequestAccessPolicy {

    private final StaffScope staffScope;
    private final ResidentApartmentRepository residentApartmentRepository;

    public RequestAccessPolicy(StaffScope staffScope, ResidentApartmentRepository residentApartmentRepository) {
        this.staffScope = staffScope;
        this.residentApartmentRepository = residentApartmentRepository;
    }

    public void checkCanView(CurrentUser user, Request request) {
        if (!canView(user, request)) {
            throw ForbiddenException.of("Request " + request.getId() + " is not available");
        }
    }

    public void checkCompanyStaff(CurrentUser user, Request request) {
        if (!staffScope.companyOf(user).equals(request.getHouse().getCompany().getId())) {
            throw ForbiddenException.of("Request " + request.getId() + " belongs to another company");
        }
    }

    private boolean canView(CurrentUser user, Request request) {
        return switch (user.role()) {
            case RESIDENT -> user.id().equals(request.getAuthor().getId())
                    || (request.getApartment() == null
                        && residentApartmentRepository.isConfirmedResidentOfHouse(user.id(), request.getHouse().getId()));
            case EMPLOYEE, COMPANY_ADMIN ->
                    staffScope.companyOf(user).equals(request.getHouse().getCompany().getId());
            case INSPECTOR -> staffScope.districtOf(user).equals(request.getHouse().getDistrict().getId());
        };
    }
}
