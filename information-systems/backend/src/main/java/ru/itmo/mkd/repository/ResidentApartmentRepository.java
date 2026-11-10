package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itmo.mkd.entity.ResidentApartment;
import ru.itmo.mkd.entity.ResidentApartmentId;

import java.util.List;

public interface ResidentApartmentRepository extends JpaRepository<ResidentApartment, ResidentApartmentId> {

    @Query("""
            select ra from ResidentApartment ra
            join fetch ra.apartment a
            join fetch a.house
            where ra.user.id = :userId
            order by a.id
            """)
    List<ResidentApartment> findByResident(@Param("userId") Long userId);

    @Query("""
            select ra from ResidentApartment ra
            join fetch ra.user
            join fetch ra.apartment a
            join fetch a.house h
            where h.company.id = :companyId and ra.confirmed = false
            order by ra.linkedAt
            """)
    List<ResidentApartment> findPendingByCompany(@Param("companyId") Long companyId);

    @Query("""
            select count(ra) > 0 from ResidentApartment ra
            where ra.user.id = :userId and ra.confirmed = true and ra.apartment.house.id = :houseId
            """)
    boolean isConfirmedResidentOfHouse(@Param("userId") Long userId, @Param("houseId") Long houseId);
}
