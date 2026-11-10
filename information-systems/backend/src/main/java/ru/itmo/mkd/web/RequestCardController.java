package ru.itmo.mkd.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.mkd.dto.CreatedDto;
import ru.itmo.mkd.dto.request.NewCommentDto;
import ru.itmo.mkd.dto.request.RequestCardDto;
import ru.itmo.mkd.security.Authenticated;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.service.CommentService;
import ru.itmo.mkd.service.RequestQueryService;

@RestController
@RequestMapping("/api/requests/{id}")
@Authenticated
public class RequestCardController {

    private final RequestQueryService queryService;
    private final CommentService commentService;

    public RequestCardController(RequestQueryService queryService, CommentService commentService) {
        this.queryService = queryService;
        this.commentService = commentService;
    }

    @GetMapping
    public RequestCardDto card(CurrentUser user, @PathVariable Long id) {
        return queryService.card(user, id);
    }

    @PostMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedDto comment(CurrentUser user, @PathVariable Long id, @Valid @RequestBody NewCommentDto dto) {
        return new CreatedDto(commentService.add(user, id, dto.body()));
    }
}
