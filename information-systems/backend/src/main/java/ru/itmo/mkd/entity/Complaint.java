package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.itmo.mkd.domain.ComplaintStatus;

import java.time.OffsetDateTime;

@Entity
@Immutable
@Table(name = "complaint")
public class Complaint {

    @Id
    @Column(name = "complaint_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resident_id")
    private AppUser resident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id")
    private Request request;

    private String body;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ComplaintStatus status;

    @Column(name = "inspector_id")
    private Long inspectorId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "decision_comment")
    private String decisionComment;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    protected Complaint() {
    }

    public Long getId() {
        return id;
    }

    public AppUser getResident() {
        return resident;
    }

    public Request getRequest() {
        return request;
    }

    public String getBody() {
        return body;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public Long getInspectorId() {
        return inspectorId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getDecisionComment() {
        return decisionComment;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getDecidedAt() {
        return decidedAt;
    }
}
