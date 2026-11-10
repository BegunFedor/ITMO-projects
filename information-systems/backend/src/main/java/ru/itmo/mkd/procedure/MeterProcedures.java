package ru.itmo.mkd.procedure;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class MeterProcedures {

    private final ProcedureExecutor executor;

    public MeterProcedures(ProcedureExecutor executor) {
        this.executor = executor;
    }

    public Long submitReading(Long meterId, Long residentId, BigDecimal value) {
        return executor.queryForLong("SELECT submit_meter_reading(?, ?, ?)", meterId, residentId, value);
    }
}
