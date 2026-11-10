package ru.itmo.mkd.procedure;

import org.springframework.stereotype.Repository;

@Repository
public class RequestProcedures {

    private final ProcedureExecutor executor;

    public RequestProcedures(ProcedureExecutor executor) {
        this.executor = executor;
    }

    public Long createRequest(Long authorId, Long houseId, Long apartmentId, Long categoryId,
                              String description, Long previousRequestId) {
        return executor.queryForLong("SELECT create_request(?, ?, ?, ?, ?, ?)",
                authorId, houseId, apartmentId, categoryId, description, previousRequestId);
    }

    public void cancelRequest(Long requestId, Long userId, String reason) {
        executor.call("CALL cancel_request(?, ?, ?)", requestId, userId, reason);
    }

    public Long addComment(Long requestId, Long authorId, String body) {
        return executor.queryForLong("SELECT add_comment(?, ?, ?)", requestId, authorId, body);
    }
}
