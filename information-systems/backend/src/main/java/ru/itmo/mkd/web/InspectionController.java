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
import ru.itmo.mkd.dto.supervision.IssueOrderDto;
import ru.itmo.mkd.dto.supervision.OrderDto;
import ru.itmo.mkd.dto.supervision.OverdueRequestDto;
import ru.itmo.mkd.dto.supervision.ReviewOrderDto;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.security.RequiresRole;
import ru.itmo.mkd.service.InspectionService;

import java.util.List;

@RestController
@RequestMapping("/api/inspection")
@RequiresRole(UserRole.INSPECTOR)
public class InspectionController {

    private final InspectionService inspectionService;

    public InspectionController(InspectionService inspectionService) {
        this.inspectionService = inspectionService;
    }

    @GetMapping("/overdue")
    public List<OverdueRequestDto> overdue(CurrentUser user) {
        return inspectionService.overdueRequests(user);
    }

    @GetMapping("/orders")
    public List<OrderDto> orders(CurrentUser user) {
        return inspectionService.orders(user);
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedDto issue(CurrentUser user, @Valid @RequestBody IssueOrderDto dto) {
        return new CreatedDto(inspectionService.issue(user, dto));
    }

    @PostMapping("/orders/{id}/review")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void review(CurrentUser user, @PathVariable Long id, @Valid @RequestBody ReviewOrderDto dto) {
        inspectionService.review(user, id, dto);
    }
}
