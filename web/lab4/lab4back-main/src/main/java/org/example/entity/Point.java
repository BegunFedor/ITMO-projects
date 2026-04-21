package org.example.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="points")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Point implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name="x", nullable = false)
    private BigDecimal x;

    @Column(name="y", nullable = false)
    private BigDecimal y;

    @Column(name="r", nullable = false)
    private Float r;

    @Column(name="hit", nullable = false)
    private Boolean hit;

    @Column(name="request_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime currentTime;

    @Column(name="execution_time", nullable = false)
    private Long executionTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Override
    public boolean equals(Object o){
        if (this==o ) return true;
        if (o==null|| getClass() != o.getClass()) return false;
        Point point = (Point) o;

        return id != null && id.equals(point.getId());
    }
}