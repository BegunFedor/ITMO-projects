package ru.itmo.mkd.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.mkd.dto.notification.NotificationDto;
import ru.itmo.mkd.dto.notification.UnreadCountDto;
import ru.itmo.mkd.security.Authenticated;
import ru.itmo.mkd.security.CurrentUser;
import ru.itmo.mkd.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Authenticated
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationDto> latest(CurrentUser user) {
        return notificationService.latest(user);
    }

    @GetMapping("/unread-count")
    public UnreadCountDto unread(CurrentUser user) {
        return notificationService.unreadCount(user);
    }

    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void readAll(CurrentUser user) {
        notificationService.markAllRead(user);
    }
}
