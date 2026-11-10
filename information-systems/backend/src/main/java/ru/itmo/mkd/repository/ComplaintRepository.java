package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itmo.mkd.entity.Complaint;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    boolean existsByRequestId(Long requestId);

    @Query("""
            select c from Complaint c
            join fetch c.resident
            join fetch c.request r
            join fetch r.category
            join fetch r.house h
            join fetch h.company
            where c.resident.id = :residentId
            order by c.createdAt desc
            """)
    List<Complaint> findByResident(@Param("residentId") Long residentId);

    @Query("""
            select c from Complaint c
            join fetch c.resident
            join fetch c.request r
            join fetch r.category
            join fetch r.house h
            join fetch h.company
            where h.district.id = :districtId
            order by c.createdAt desc
            """)
    List<Complaint> findByDistrict(@Param("districtId") Long districtId);
}
