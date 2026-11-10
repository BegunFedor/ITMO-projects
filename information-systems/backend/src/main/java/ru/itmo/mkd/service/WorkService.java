package ru.itmo.mkd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.procedure.WorkProcedures;
import ru.itmo.mkd.security.CurrentUser;

@Service
public class WorkService {

    private final WorkProcedures workProcedures;

    public WorkService(WorkProcedures workProcedures) {
        this.workProcedures = workProcedures;
    }

    @Transactional
    public void start(CurrentUser employee, Long requestId) {
        workProcedures.startWork(requestId, employee.id());
    }

    @Transactional
    public void complete(CurrentUser employee, Long requestId, String result) {
        workProcedures.completeRequest(requestId, employee.id(), result.trim());
    }
}
