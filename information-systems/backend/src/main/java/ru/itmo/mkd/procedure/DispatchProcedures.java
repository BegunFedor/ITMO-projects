package ru.itmo.mkd.procedure;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DispatchProcedures {

    private final ProcedureExecutor executor;

    public DispatchProcedures(ProcedureExecutor executor) {
        this.executor = executor;
    }

    public void acceptRequest(Long requestId, Long adminId) {
        executor.call("CALL accept_request(?, ?)", requestId, adminId);
    }

    public List<ExecutorCandidate> suggestExecutors(Long requestId) {
        return executor.query("SELECT * FROM suggest_executors(?)",
                (rs, i) -> new ExecutorCandidate(rs.getLong("executor_id"), rs.getString("full_name"),
                        rs.getBoolean("matches_specialization"), rs.getLong("active_requests")),
                requestId);
    }

    public void assignExecutor(Long requestId, Long adminId, Long executorId, String comment) {
        executor.call("CALL assign_executor(?, ?, ?, ?)", requestId, adminId, executorId, comment);
    }
}
