package ru.itmo.mkd.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.procedure.RequestProcedures;
import ru.itmo.mkd.security.CurrentUser;

@Service
public class CommentService {

    private final RequestProcedures requestProcedures;

    public CommentService(RequestProcedures requestProcedures) {
        this.requestProcedures = requestProcedures;
    }

    @Transactional
    public Long add(CurrentUser author, Long requestId, String body) {
        return requestProcedures.addComment(requestId, author.id(), body.trim());
    }
}
