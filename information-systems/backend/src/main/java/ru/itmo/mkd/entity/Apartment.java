package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "apartment")
public class Apartment {

    @Id
    @Column(name = "apartment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "house_id")
    private House house;

    private String number;

    private Short entrance;

    private Short floor;

    protected Apartment() {
    }

    public Long getId() {
        return id;
    }

    public House getHouse() {
        return house;
    }

    public String getNumber() {
        return number;
    }

    public Short getEntrance() {
        return entrance;
    }

    public Short getFloor() {
        return floor;
    }
}
