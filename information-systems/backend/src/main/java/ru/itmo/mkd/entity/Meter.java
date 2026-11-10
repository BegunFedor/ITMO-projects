package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.itmo.mkd.domain.ResourceType;

import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "meter")
public class Meter {

    @Id
    @Column(name = "meter_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private ResourceType resource;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "verified_until")
    private LocalDate verifiedUntil;

    @Column(name = "is_active")
    private boolean active;

    protected Meter() {
    }

    public Long getId() {
        return id;
    }

    public Apartment getApartment() {
        return apartment;
    }

    public ResourceType getResource() {
        return resource;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public LocalDate getVerifiedUntil() {
        return verifiedUntil;
    }

    public boolean isActive() {
        return active;
    }
}
