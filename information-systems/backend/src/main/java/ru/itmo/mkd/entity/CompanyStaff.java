package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.util.Set;

@Entity
@Immutable
@Table(name = "company_staff")
public class CompanyStaff {

    @Id
    @Column(name = "user_id")
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    private ManagementCompany company;

    private String position;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "staff_specialization",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id"))
    private Set<Specialization> specializations;

    protected CompanyStaff() {
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public ManagementCompany getCompany() {
        return company;
    }

    public String getPosition() {
        return position;
    }

    public Set<Specialization> getSpecializations() {
        return specializations;
    }
}
