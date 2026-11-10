package ru.itmo.mkd.procedure;

import org.springframework.stereotype.Repository;

@Repository
public class SchedulerProcedures {

    private final ProcedureExecutor executor;

    public SchedulerProcedures(ProcedureExecutor executor) {
        this.executor = executor;
    }

    public int markOverdueRequests() {
        return executor.queryForInt("SELECT mark_overdue_requests()");
    }

    public int autoCloseUnrated(int days) {
        return executor.queryForInt("SELECT auto_close_unrated(?)", days);
    }

    public int markOverdueOrders() {
        return executor.queryForInt("SELECT mark_overdue_orders()");
    }
}
