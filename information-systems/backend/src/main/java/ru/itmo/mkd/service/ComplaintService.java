package ru.itmo.mkd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.dto.supervision.ComplaintDto;
import ru.itmo.mkd.dto.supervision.FileComplaintDto;
import ru.itmo.mkd.mapper.SupervisionMapper;
import ru.itmo.mkd.procedure.ComplaintProcedures;
import ru.itmo.mkd.repository.ComplaintRepository;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.service.access.StaffScope;

import java.util.List;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintProcedures complaintProcedures;
    private final StaffScope staffScope;
    private final SupervisionMapper mapper;

    public ComplaintService(ComplaintRepository complaintRepository, ComplaintProcedures complaintProcedures,
                            StaffScope staffScope, SupervisionMapper mapper) {
        this.complaintRepository = complaintRepository;
        this.complaintProcedures = complaintProcedures;
        this.staffScope = staffScope;
        this.mapper = mapper;
    }

    @Transactional
    public Long file(CurrentUser resident, FileComplaintDto dto) {
        return complaintProcedures.fileComplaint(resident.id(), dto.requestId(), dto.body().trim());
    }

    @Transactional(readOnly = true)
    public List<ComplaintDto> residentComplaints(CurrentUser resident) {
        return mapper.toComplaintDtos(complaintRepository.findByResident(resident.id()));
    }

    @Transactional(readOnly = true)
    public List<ComplaintDto> districtComplaints(CurrentUser inspector) {
        return mapper.toComplaintDtos(complaintRepository.findByDistrict(staffScope.districtOf(inspector)));
    }

    @Transactional
    public void reject(CurrentUser inspector, Long complaintId, String reason) {
        complaintProcedures.rejectComplaint(inspector.id(), complaintId, reason.trim());
    }
}
