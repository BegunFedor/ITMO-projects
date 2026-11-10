package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itmo.mkd.entity.Announcement;

import java.time.OffsetDateTime;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    @Query("""
            select distinct a from Announcement a
            join a.houses h
            where h.id in (select ap.house.id from ResidentApartment ra join ra.apartment ap
                           where ra.user.id = :residentId and ra.confirmed = true)
              and a.visibleFrom <= :now
              and (a.visibleTo is null or a.visibleTo > :now)
            order by a.visibleFrom desc
            """)
    List<Announcement> findVisibleForResident(@Param("residentId") Long residentId,
                                              @Param("now") OffsetDateTime now);

    @Query("""
            select distinct a from Announcement a
            left join fetch a.houses
            where a.company.id = :companyId
            order by a.createdAt desc
            """)
    List<Announcement> findByCompany(@Param("companyId") Long companyId);
}
