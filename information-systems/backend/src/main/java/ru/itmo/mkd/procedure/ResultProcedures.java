package ru.itmo.mkd.procedure;

import org.springframework.stereotype.Repository;

@Repository
public class ResultProcedures {

    private final ProcedureExecutor executor;

    public ResultProcedures(ProcedureExecutor executor) {
        this.executor = executor;
    }

    public void rateRequest(Long requestId, Long residentId, short score, String review) {
        executor.call("CALL rate_request(?, ?, ?::smallint, ?)", requestId, residentId, score, review);
    }

    public void rejectResult(Long requestId, Long residentId, String reason) {
        executor.call("CALL reject_result(?, ?, ?)", requestId, residentId, reason);
    }
}
