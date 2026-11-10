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
import ru.itmo.mkd.dto.request.CompleteDto;
import ru.itmo.mkd.dto.request.RequestSummaryDto;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.security.RequiresRole;
import ru.itmo.mkd.service.RequestQueryService;
import ru.itmo.mkd.service.WorkService;

import java.util.List;

@RestController
@RequestMapping("/api/work/tasks")
@RequiresRole(UserRole.EMPLOYEE)
public class WorkController {

    private final RequestQueryService queryService;
    private final WorkService workService;

    public WorkController(RequestQueryService queryService, WorkService workService) {
        this.queryService = queryService;
        this.workService = workService;
    }

    @GetMapping
    public List<RequestSummaryDto> tasks(CurrentUser user) {
        return queryService.executorTasks(user);
    }

    @PostMapping("/{id}/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void start(CurrentUser user, @PathVariable Long id) {
        workService.start(user, id);
    }

    @PostMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void complete(CurrentUser user, @PathVariable Long id, @Valid @RequestBody CompleteDto dto) {
        workService.complete(user, id, dto.result());
    }
}
