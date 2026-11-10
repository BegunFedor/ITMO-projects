package ru.itmo.mkd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.dto.reference.ConfirmResidentRequest;
import ru.itmo.mkd.dto.reference.PendingResidentDto;
import ru.itmo.mkd.mapper.ReferenceMapper;
import ru.itmo.mkd.procedure.AccountProcedures;
import ru.itmo.mkd.repository.ResidentApartmentRepository;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.service.access.StaffScope;

import java.util.List;

@Service
public class ResidentConfirmationService {

    private final ResidentApartmentRepository residentApartmentRepository;
    private final AccountProcedures accountProcedures;
    private final StaffScope staffScope;
    private final ReferenceMapper mapper;

    public ResidentConfirmationService(ResidentApartmentRepository residentApartmentRepository,
                                       AccountProcedures accountProcedures, StaffScope staffScope,
                                       ReferenceMapper mapper) {
        this.residentApartmentRepository = residentApartmentRepository;
        this.accountProcedures = accountProcedures;
        this.staffScope = staffScope;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<PendingResidentDto> pending(CurrentUser admin) {
        return mapper.toPendingList(residentApartmentRepository.findPendingByCompany(staffScope.companyOf(admin)));
    }

    @Transactional
    public void confirm(CurrentUser admin, ConfirmResidentRequest request) {
        accountProcedures.confirmResident(admin.id(), request.userId(), request.apartmentId());
    }
}
