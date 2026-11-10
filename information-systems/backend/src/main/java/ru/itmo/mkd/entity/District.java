package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "district")
public class District {

    @Id
    @Column(name = "district_id")
    private Long id;

    private String name;

    protected District() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
