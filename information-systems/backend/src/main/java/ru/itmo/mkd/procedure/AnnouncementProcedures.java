package ru.itmo.mkd.procedure;

import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class AnnouncementProcedures {

    private final ProcedureExecutor executor;

    public AnnouncementProcedures(ProcedureExecutor executor) {
        this.executor = executor;
    }

    public Long publish(Long adminId, String title, String body, List<Long> houseIds,
                        OffsetDateTime visibleFrom, OffsetDateTime visibleTo) {
        return executor.queryForLong("SELECT publish_announcement(?, ?, ?, ?, ?, ?)",
                adminId, title, body, executor.bigintArray(houseIds), visibleFrom, visibleTo);
    }
}
