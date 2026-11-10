package ru.itmo.mkd.mapper;

import org.mapstruct.Mapper;
import ru.itmo.mkd.dto.notification.NotificationDto;
import ru.itmo.mkd.entity.Notification;

import java.util.List;

@Mapper
public interface NotificationMapper {

    NotificationDto toDto(Notification notification);

    List<NotificationDto> toDtos(List<Notification> notifications);
}
