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
import ru.itmo.mkd.dto.request.CreateRequestDto;
import ru.itmo.mkd.dto.request.RateDto;
import ru.itmo.mkd.dto.request.ReasonDto;
import ru.itmo.mkd.dto.request.RequestSummaryDto;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.security.RequiresRole;
import ru.itmo.mkd.service.RequestQueryService;
import ru.itmo.mkd.service.ResidentRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/resident/requests")
@RequiresRole(UserRole.RESIDENT)
public class ResidentRequestController {

    private final RequestQueryService queryService;
    private final ResidentRequestService residentRequestService;

    public ResidentRequestController(RequestQueryService queryService, ResidentRequestService residentRequestService) {
        this.queryService = queryService;
        this.residentRequestService = residentRequestService;
    }

    @GetMapping
    public List<RequestSummaryDto> list(CurrentUser user) {
        return queryService.residentRequests(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedDto create(CurrentUser user, @Valid @RequestBody CreateRequestDto dto) {
        return new CreatedDto(residentRequestService.create(user, dto));
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(CurrentUser user, @PathVariable Long id, @Valid @RequestBody ReasonDto dto) {
        residentRequestService.cancel(user, id, dto.reason());
    }

    @PostMapping("/{id}/rating")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rate(CurrentUser user, @PathVariable Long id, @Valid @RequestBody RateDto dto) {
        residentRequestService.rate(user, id, dto);
    }

    @PostMapping("/{id}/reject-result")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectResult(CurrentUser user, @PathVariable Long id, @Valid @RequestBody ReasonDto dto) {
        residentRequestService.rejectResult(user, id, dto.reason());
    }
}
