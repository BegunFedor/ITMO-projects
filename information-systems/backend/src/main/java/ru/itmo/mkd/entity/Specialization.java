package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "specialization")
public class Specialization {

    @Id
    @Column(name = "specialization_id")
    private Long id;

    private String name;

    protected Specialization() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
