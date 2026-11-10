package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itmo.mkd.entity.Inspector;

import java.util.Optional;

public interface InspectorRepository extends JpaRepository<Inspector, Long> {

    @Query("select i.district.id from Inspector i where i.id = :userId")
    Optional<Long> findDistrictId(@Param("userId") Long userId);
}
