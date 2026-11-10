package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itmo.mkd.entity.CompanyStaff;

import java.util.Optional;

public interface CompanyStaffRepository extends JpaRepository<CompanyStaff, Long> {

    @Query("select s.company.id from CompanyStaff s where s.id = :userId")
    Optional<Long> findCompanyId(@Param("userId") Long userId);
}
