package ru.itmo.mkd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.dto.request.AssignDto;
import ru.itmo.mkd.dto.request.ExecutorOptionDto;
import ru.itmo.mkd.mapper.RequestMapper;
import ru.itmo.mkd.procedure.DispatchProcedures;
import ru.itmo.mkd.procedure.RequestProcedures;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.service.access.RequestAccessPolicy;

import java.util.List;

@Service
public class DispatchService {

    private final DispatchProcedures dispatchProcedures;
    private final RequestProcedures requestProcedures;
    private final RequestQueryService requestQueryService;
    private final RequestAccessPolicy accessPolicy;
    private final RequestMapper mapper;

    public DispatchService(DispatchProcedures dispatchProcedures, RequestProcedures requestProcedures,
                           RequestQueryService requestQueryService, RequestAccessPolicy accessPolicy,
                           RequestMapper mapper) {
        this.dispatchProcedures = dispatchProcedures;
        this.requestProcedures = requestProcedures;
        this.requestQueryService = requestQueryService;
        this.accessPolicy = accessPolicy;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ExecutorOptionDto> suggestExecutors(CurrentUser admin, Long requestId) {
        accessPolicy.checkCompanyStaff(admin, requestQueryService.load(requestId));
        return mapper.toExecutorOptions(dispatchProcedures.suggestExecutors(requestId));
    }

    @Transactional
    public void accept(CurrentUser admin, Long requestId) {
        dispatchProcedures.acceptRequest(requestId, admin.id());
    }

    @Transactional
    public void assign(CurrentUser admin, Long requestId, AssignDto dto) {
        String comment = dto.comment() == null || dto.comment().isBlank() ? null : dto.comment().trim();
        dispatchProcedures.assignExecutor(requestId, admin.id(), dto.executorId(), comment);
    }

    @Transactional
    public void cancel(CurrentUser admin, Long requestId, String reason) {
        requestProcedures.cancelRequest(requestId, admin.id(), reason.trim());
    }
}
