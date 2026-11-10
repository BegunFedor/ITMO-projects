package ru.itmo.mkd.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.mkd.domain.UserRole;
import ru.itmo.mkd.dto.CreatedDto;
import ru.itmo.mkd.dto.request.ReasonDto;
import ru.itmo.mkd.dto.supervision.ComplaintDto;
import ru.itmo.mkd.dto.supervision.FileComplaintDto;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.security.RequiresRole;
import ru.itmo.mkd.service.ComplaintService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/resident/complaints")
    @RequiresRole(UserRole.RESIDENT)
    public List<ComplaintDto> mine(CurrentUser user) {
        return complaintService.residentComplaints(user);
    }

    @PostMapping("/resident/complaints")
    @RequiresRole(UserRole.RESIDENT)
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedDto file(CurrentUser user, @Valid @RequestBody FileComplaintDto dto) {
        return new CreatedDto(complaintService.file(user, dto));
    }

    @GetMapping("/inspection/complaints")
    @RequiresRole(UserRole.INSPECTOR)
    public List<ComplaintDto> district(CurrentUser user) {
        return complaintService.districtComplaints(user);
    }

    @PostMapping("/inspection/complaints/{id}/reject")
    @RequiresRole(UserRole.INSPECTOR)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(CurrentUser user, @PathVariable Long id, @Valid @RequestBody ReasonDto dto) {
        complaintService.reject(user, id, dto.reason());
    }
}
