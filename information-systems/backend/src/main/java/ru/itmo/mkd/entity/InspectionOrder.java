package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.itmo.mkd.domain.OrderStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Immutable
@Table(name = "inspection_order")
public class InspectionOrder {

    @Id
    @Column(name = "order_id")
    private Long id;

    private String number;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspector_id")
    private AppUser inspector;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    private ManagementCompany company;

    private String violation;

    @Column(name = "issued_at")
    private OffsetDateTime issuedAt;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private OrderStatus status;

    @Column(name = "report_text")
    private String reportText;

    @Column(name = "report_at")
    private OffsetDateTime reportAt;

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    @Column(name = "review_comment")
    private String reviewComment;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "order_request",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "request_id"))
    private Set<Request> requests;

    protected InspectionOrder() {
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public AppUser getInspector() {
        return inspector;
    }

    public ManagementCompany getCompany() {
        return company;
    }

    public String getViolation() {
        return violation;
    }

    public OffsetDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getReportText() {
        return reportText;
    }

    public OffsetDateTime getReportAt() {
        return reportAt;
    }

    public OffsetDateTime getReviewedAt() {
        return reviewedAt;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public Set<Request> getRequests() {
        return requests;
    }
}
