package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "management_company")
public class ManagementCompany {

    @Id
    @Column(name = "company_id")
    private Long id;

    private String name;

    private String inn;

    private String address;

    @Column(name = "dispatch_phone")
    private String dispatchPhone;

    protected ManagementCompany() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getInn() {
        return inn;
    }

    public String getAddress() {
        return address;
    }

    public String getDispatchPhone() {
        return dispatchPhone;
    }
}
