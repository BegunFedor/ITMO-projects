package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.time.OffsetDateTime;

@Entity
@Immutable
@Table(name = "rating")
public class Rating {

    @Id
    @Column(name = "request_id")
    private Long requestId;

    private Short score;

    private String review;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    protected Rating() {
    }

    public Long getRequestId() {
        return requestId;
    }

    public Short getScore() {
        return score;
    }

    public String getReview() {
        return review;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
