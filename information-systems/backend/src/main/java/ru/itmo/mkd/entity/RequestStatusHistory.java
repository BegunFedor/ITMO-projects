package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.itmo.mkd.domain.RequestStatus;

import java.time.OffsetDateTime;

@Entity
@Immutable
@Table(name = "request_status_history")
public class RequestStatusHistory {

    @Id
    @Column(name = "history_id")
    private Long id;

    @Column(name = "request_id")
    private Long requestId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private AppUser changedBy;

    @Column(name = "changed_at")
    private OffsetDateTime changedAt;

    private String comment;

    protected RequestStatusHistory() {
    }

    public Long getId() {
        return id;
    }

    public Long getRequestId() {
        return requestId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public AppUser getChangedBy() {
        return changedBy;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }

    public String getComment() {
        return comment;
    }
}
