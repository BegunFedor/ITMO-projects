package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import ru.itmo.mkd.domain.OwnershipType;

import java.time.OffsetDateTime;

@Entity
@Immutable
@Table(name = "resident_apartment")
public class ResidentApartment {

    @EmbeddedId
    private ResidentApartmentId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @MapsId("apartmentId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private OwnershipType ownership;

    @Column(name = "is_confirmed")
    private boolean confirmed;

    @Column(name = "linked_at")
    private OffsetDateTime linkedAt;

    protected ResidentApartment() {
    }

    public ResidentApartmentId getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Apartment getApartment() {
        return apartment;
    }

    public OwnershipType getOwnership() {
        return ownership;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public OffsetDateTime getLinkedAt() {
        return linkedAt;
    }
}
