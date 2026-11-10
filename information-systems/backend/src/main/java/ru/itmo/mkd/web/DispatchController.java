package ru.itmo.mkd.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.mkd.domain.RequestStatus;
import ru.itmo.mkd.domain.UserRole;
import ru.itmo.mkd.dto.request.AssignDto;
import ru.itmo.mkd.dto.request.ExecutorOptionDto;
import ru.itmo.mkd.dto.request.ReasonDto;
import ru.itmo.mkd.dto.request.RequestSummaryDto;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.security.RequiresRole;
import ru.itmo.mkd.service.DispatchService;
import ru.itmo.mkd.service.RequestQueryService;

import java.util.List;

@RestController
@RequestMapping("/api/dispatch/requests")
@RequiresRole(UserRole.COMPANY_ADMIN)
public class DispatchController {

    private final RequestQueryService queryService;
    private final DispatchService dispatchService;

    public DispatchController(RequestQueryService queryService, DispatchService dispatchService) {
        this.queryService = queryService;
        this.dispatchService = dispatchService;
    }

    @GetMapping
    public List<RequestSummaryDto> list(CurrentUser user,
                                        @RequestParam(name = "status", required = false) List<RequestStatus> statuses) {
        return queryService.companyRequests(user, statuses);
    }

    @GetMapping("/{id}/executors")
    public List<ExecutorOptionDto> executors(CurrentUser user, @PathVariable Long id) {
        return dispatchService.suggestExecutors(user, id);
    }

    @PostMapping("/{id}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void accept(CurrentUser user, @PathVariable Long id) {
        dispatchService.accept(user, id);
    }

    @PostMapping("/{id}/assign")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assign(CurrentUser user, @PathVariable Long id, @Valid @RequestBody AssignDto dto) {
        dispatchService.assign(user, id, dto);
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(CurrentUser user, @PathVariable Long id, @Valid @RequestBody ReasonDto dto) {
        dispatchService.cancel(user, id, dto.reason());
    }
}
