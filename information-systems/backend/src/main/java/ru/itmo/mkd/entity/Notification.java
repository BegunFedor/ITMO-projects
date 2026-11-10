package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.itmo.mkd.domain.NotificationEvent;
import ru.itmo.mkd.domain.NotificationTarget;
import ru.itmo.mkd.domain.RequestStatus;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notification")
public class Notification {

    @Id
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    private NotificationEvent event;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type")
    private NotificationTarget entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private RequestStatus status;

    @Column(name = "is_read")
    private boolean read;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    protected Notification() {
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public NotificationEvent getEvent() {
        return event;
    }

    public NotificationTarget getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public boolean isRead() {
        return read;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
