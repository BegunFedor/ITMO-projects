package ru.itmo.mkd.procedure;

import org.springframework.stereotype.Repository;

@Repository
public class WorkProcedures {

    private final ProcedureExecutor executor;

    public WorkProcedures(ProcedureExecutor executor) {
        this.executor = executor;
    }

    public void startWork(Long requestId, Long executorId) {
        executor.call("CALL start_work(?, ?)", requestId, executorId);
    }

    public void completeRequest(Long requestId, Long executorId, String result) {
        executor.call("CALL complete_request(?, ?, ?)", requestId, executorId, result);
    }
}
