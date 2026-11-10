package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "request_category")
public class RequestCategory {

    @Id
    @Column(name = "category_id")
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "specialization_id")
    private Specialization specialization;

    @Column(name = "norm_hours")
    private Integer normHours;

    @Column(name = "is_emergency")
    private boolean emergency;

    @Column(name = "is_active")
    private boolean active;

    protected RequestCategory() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Specialization getSpecialization() {
        return specialization;
    }

    public Integer getNormHours() {
        return normHours;
    }

    public boolean isEmergency() {
        return emergency;
    }

    public boolean isActive() {
        return active;
    }
}
