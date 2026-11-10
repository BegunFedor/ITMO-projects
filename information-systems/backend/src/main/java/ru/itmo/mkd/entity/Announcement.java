package ru.itmo.mkd.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Immutable
@Table(name = "announcement")
public class Announcement {

    @Id
    @Column(name = "announcement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id")
    private ManagementCompany company;

    private String title;

    private String body;

    @Column(name = "visible_from")
    private OffsetDateTime visibleFrom;

    @Column(name = "visible_to")
    private OffsetDateTime visibleTo;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "announcement_house",
            joinColumns = @JoinColumn(name = "announcement_id"),
            inverseJoinColumns = @JoinColumn(name = "house_id"))
    private Set<House> houses;

    protected Announcement() {
    }

    public Long getId() {
        return id;
    }

    public ManagementCompany getCompany() {
        return company;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public OffsetDateTime getVisibleFrom() {
        return visibleFrom;
    }

    public OffsetDateTime getVisibleTo() {
        return visibleTo;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Set<House> getHouses() {
        return houses;
    }
}
