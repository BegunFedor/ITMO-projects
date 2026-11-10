package ru.itmo.mkd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ResidentApartmentId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "apartment_id")
    private Long apartmentId;

    protected ResidentApartmentId() {
    }

    public Long getUserId() {
        return userId;
    }

    public Long getApartmentId() {
        return apartmentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResidentApartmentId other)) return false;
        return Objects.equals(userId, other.userId) && Objects.equals(apartmentId, other.apartmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, apartmentId);
    }
}
