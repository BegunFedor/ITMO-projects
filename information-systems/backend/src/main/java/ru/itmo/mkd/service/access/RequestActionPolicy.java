package ru.itmo.mkd.service.access;

import org.springframework.stereotype.Component;
import ru.itmo.mkd.domain.RequestStatus;
import ru.itmo.mkd.dto.request.RequestAction;
import ru.itmo.mkd.entity.Rating;
import ru.itmo.mkd.entity.Request;
import ru.itmo.mkd.entity.RequestStatusHistory;
import ru.itmo.mkd.security.CurrentUser;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
public class RequestActionPolicy {

    private static final int LOW_SCORE = 2;

    public Set<RequestAction> actionsFor(CurrentUser user, Request request, Rating rating,
                                         boolean hasComplaint, List<RequestStatusHistory> history) {
        Set<RequestAction> actions = EnumSet.noneOf(RequestAction.class);
        RequestStatus status = request.getStatus();
        boolean open = status != RequestStatus.CLOSED && status != RequestStatus.CANCELLED;
        switch (user.role()) {
            case RESIDENT -> {
                if (!user.id().equals(request.getAuthor().getId())) {
                    break;
                }
                if (status == RequestStatus.NEW) actions.add(RequestAction.CANCEL);
                if (status == RequestStatus.DONE) {
                    actions.add(RequestAction.RATE);
                    actions.add(RequestAction.REJECT_RESULT);
                }
                if (status == RequestStatus.CANCELLED) actions.add(RequestAction.CREATE_REPEAT);
                if (open) actions.add(RequestAction.COMMENT);
                if (!hasComplaint && complaintAllowed(request, rating, history)) actions.add(RequestAction.COMPLAIN);
            }
            case COMPANY_ADMIN -> {
                if (status == RequestStatus.NEW) actions.add(RequestAction.ACCEPT);
                if (status == RequestStatus.ACCEPTED) actions.add(RequestAction.ASSIGN);
                if (status == RequestStatus.NEW || status == RequestStatus.ACCEPTED) actions.add(RequestAction.CANCEL);
                if (open) actions.add(RequestAction.COMMENT);
            }
            case EMPLOYEE -> {
                boolean executor = request.getExecutor() != null && user.id().equals(request.getExecutor().getId());
                if (executor && status == RequestStatus.ASSIGNED) actions.add(RequestAction.START);
                if (executor && status == RequestStatus.IN_PROGRESS) actions.add(RequestAction.COMPLETE);
                if (open) actions.add(RequestAction.COMMENT);
            }
            case INSPECTOR -> {
            }
        }
        return actions;
    }

    private static boolean complaintAllowed(Request request, Rating rating, List<RequestStatusHistory> history) {
        if (request.isOverdue()) {
            return true;
        }
        if (request.getStatus() == RequestStatus.CLOSED) {
            return rating != null && rating.getScore() <= LOW_SCORE;
        }
        if (request.getStatus() == RequestStatus.CANCELLED && history.size() >= 2) {
            RequestStatusHistory cancel = history.get(history.size() - 1);
            RequestStatus before = history.get(history.size() - 2).getStatus();
            return before == RequestStatus.DONE || cancel.getChangedBy() == null
                    || !cancel.getChangedBy().getId().equals(request.getAuthor().getId());
        }
        return false;
    }
}
