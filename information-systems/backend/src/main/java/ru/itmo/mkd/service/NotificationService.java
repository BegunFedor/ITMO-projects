package ru.itmo.mkd.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.mkd.dto.notification.NotificationDto;
import ru.itmo.mkd.dto.notification.UnreadCountDto;
import ru.itmo.mkd.mapper.NotificationMapper;
import ru.itmo.mkd.repository.NotificationRepository;
import ru.itmo.mkd.security.CurrentUser;

import java.util.List;

@Service
public class NotificationService {

    private static final int PAGE_SIZE = 50;

    private final NotificationRepository notificationRepository;
    private final NotificationMapper mapper;

    public NotificationService(NotificationRepository notificationRepository, NotificationMapper mapper) {
        this.notificationRepository = notificationRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> latest(CurrentUser user) {
        return mapper.toDtos(notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(
                user.id(), PageRequest.of(0, PAGE_SIZE)));
    }

    @Transactional(readOnly = true)
    public UnreadCountDto unreadCount(CurrentUser user) {
        return new UnreadCountDto(notificationRepository.countByUserIdAndReadFalse(user.id()));
    }

    @Transactional
    public void markAllRead(CurrentUser user) {
        notificationRepository.markAllRead(user.id());
    }
}
