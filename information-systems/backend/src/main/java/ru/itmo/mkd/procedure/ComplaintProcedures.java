package ru.itmo.mkd.procedure;

import org.springframework.stereotype.Repository;

@Repository
public class ComplaintProcedures {

    private final ProcedureExecutor executor;

    public ComplaintProcedures(ProcedureExecutor executor) {
        this.executor = executor;
    }

    public Long fileComplaint(Long residentId, Long requestId, String body) {
        return executor.queryForLong("SELECT file_complaint(?, ?, ?)", residentId, requestId, body);
    }

    public void rejectComplaint(Long inspectorId, Long complaintId, String reason) {
        executor.call("CALL reject_complaint(?, ?, ?)", inspectorId, complaintId, reason);
    }
}
