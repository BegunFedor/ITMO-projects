package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.itmo.mkd.domain.RequestStatus;

import java.time.OffsetDateTime;

@Entity
@Immutable
@Table(name = "v_request")
public class Request {

    @Id
    @Column(name = "request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private AppUser author;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_id")
    private House house;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private RequestCategory category;

    @Column(name = "previous_request_id")
    private Long previousRequestId;

    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executor_id")
    private AppUser executor;

    @Column(name = "result_text")
    private String resultText;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "deadline_at")
    private OffsetDateTime deadlineAt;

    @Column(name = "status_changed_at")
    private OffsetDateTime statusChangedAt;

    @Column(name = "is_overdue")
    private boolean overdue;

    @Version
    private Integer version;

    protected Request() {
    }

    public Long getId() {
        return id;
    }

    public AppUser getAuthor() {
        return author;
    }

    public House getHouse() {
        return house;
    }

    public Apartment getApartment() {
        return apartment;
    }

    public RequestCategory getCategory() {
        return category;
    }

    public Long getPreviousRequestId() {
        return previousRequestId;
    }

    public String getDescription() {
        return description;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public AppUser getExecutor() {
        return executor;
    }

    public String getResultText() {
        return resultText;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getDeadlineAt() {
        return deadlineAt;
    }

    public OffsetDateTime getStatusChangedAt() {
        return statusChangedAt;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public Integer getVersion() {
        return version;
    }
}
