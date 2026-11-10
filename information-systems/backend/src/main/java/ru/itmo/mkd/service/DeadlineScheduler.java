package ru.itmo.mkd.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.procedure.SchedulerProcedures;

@Component
public class DeadlineScheduler {

    private static final Logger log = LoggerFactory.getLogger(DeadlineScheduler.class);

    private final SchedulerProcedures schedulerProcedures;
    private final int rateDays;

    public DeadlineScheduler(SchedulerProcedures schedulerProcedures,
                             @Value("${mkd.scheduler.rate-days:7}") int rateDays) {
        this.schedulerProcedures = schedulerProcedures;
        this.rateDays = rateDays;
    }

    @Scheduled(initialDelayString = "${mkd.scheduler.initial-delay:PT10S}",
            fixedDelayString = "${mkd.scheduler.overdue-delay:PT5M}")
    @Transactional
    public void markOverdue() {
        int requests = schedulerProcedures.markOverdueRequests();
        int orders = schedulerProcedures.markOverdueOrders();
        if (requests + orders > 0) {
            log.info("Marked overdue: {} requests, {} orders", requests, orders);
        }
    }

    @Scheduled(cron = "${mkd.scheduler.auto-close-cron:0 15 3 * * *}")
    @Transactional
    public void closeUnrated() {
        int closed = schedulerProcedures.autoCloseUnrated(rateDays);
        if (closed > 0) {
            log.info("Closed {} unrated requests", closed);
        }
    }
}
