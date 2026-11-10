package ru.itmo.mkd.service.access;

import org.springframework.stereotype.Component;
import ru.itmo.mkd.exception.ForbiddenException;
import ru.itmo.mkd.repository.CompanyStaffRepository;
import ru.itmo.mkd.repository.InspectorRepository;
import ru.itmo.mkd.security.CurrentUser;

@Component
public class StaffScope {

    private final CompanyStaffRepository staffRepository;
    private final InspectorRepository inspectorRepository;

    public StaffScope(CompanyStaffRepository staffRepository, InspectorRepository inspectorRepository) {
        this.staffRepository = staffRepository;
        this.inspectorRepository = inspectorRepository;
    }

    public Long companyOf(CurrentUser user) {
        return staffRepository.findCompanyId(user.id())
                .orElseThrow(() -> ForbiddenException.of("User is not a company employee"));
    }

    public Long districtOf(CurrentUser user) {
        return inspectorRepository.findDistrictId(user.id())
                .orElseThrow(() -> ForbiddenException.of("User is not an inspector"));
    }
}
