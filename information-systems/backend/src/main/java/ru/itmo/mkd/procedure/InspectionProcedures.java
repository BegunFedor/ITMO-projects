package ru.itmo.mkd.procedure;

import org.springframework.stereotype.Repository;
import ru.itmo.mkd.domain.RequestStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class InspectionProcedures {

    private final ProcedureExecutor executor;

    public InspectionProcedures(ProcedureExecutor executor) {
        this.executor = executor;
    }

    public List<OverdueRequestRow> overdueRequests(Long inspectorId) {
        return executor.query("SELECT * FROM inspector_overdue_requests(?)",
                (rs, i) -> new OverdueRequestRow(
                        rs.getLong("request_id"), rs.getLong("company_id"), rs.getString("company"),
                        rs.getString("address"), rs.getString("category"),
                        RequestStatus.valueOf(rs.getString("status")),
                        rs.getObject("created_at", OffsetDateTime.class),
                        rs.getObject("deadline_at", OffsetDateTime.class),
                        rs.getBigDecimal("hours_overdue"), rs.getBoolean("has_complaint")),
                inspectorId);
    }

    public Long issueOrder(Long inspectorId, Long companyId, String violation, LocalDate dueDate,
                           List<Long> requestIds, List<Long> complaintIds) {
        return executor.queryForLong("SELECT issue_order(?, ?, ?, ?, ?, ?)",
                inspectorId, companyId, violation, dueDate,
                executor.bigintArray(requestIds), executor.bigintArray(complaintIds));
    }

    public void reviewOrder(Long orderId, Long inspectorId, boolean accepted, String comment) {
        executor.call("CALL review_order(?, ?, ?, ?)", orderId, inspectorId, accepted, comment);
    }

    public void submitOrderReport(Long orderId, Long adminId, String report) {
        executor.call("CALL submit_order_report(?, ?, ?)", orderId, adminId, report);
    }
}
