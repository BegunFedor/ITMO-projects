package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "house")
public class House {

    @Id
    @Column(name = "house_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    private ManagementCompany company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "district_id")
    private District district;

    private String address;

    @Column(name = "build_year")
    private Short buildYear;

    private Short floors;

    protected House() {
    }

    public Long getId() {
        return id;
    }

    public ManagementCompany getCompany() {
        return company;
    }

    public District getDistrict() {
        return district;
    }

    public String getAddress() {
        return address;
    }

    public Short getBuildYear() {
        return buildYear;
    }

    public Short getFloors() {
        return floors;
    }
}
