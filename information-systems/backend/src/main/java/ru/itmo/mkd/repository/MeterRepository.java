package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itmo.mkd.entity.Meter;

import java.util.List;

public interface MeterRepository extends JpaRepository<Meter, Long> {

    @Query("""
            select m from Meter m
            join fetch m.apartment a
            join fetch a.house
            where m.active = true
              and a.id in (select ra.apartment.id from ResidentApartment ra
                           where ra.user.id = :residentId and ra.confirmed = true)
            order by a.id, m.resource
            """)
    List<Meter> findActiveForResident(@Param("residentId") Long residentId);
}
