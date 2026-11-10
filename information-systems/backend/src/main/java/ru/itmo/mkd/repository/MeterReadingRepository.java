package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itmo.mkd.entity.MeterReading;

import java.util.Collection;
import java.util.List;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {

    @Query("""
            select r from MeterReading r
            where r.meterId in :meterIds
              and r.readingDate = (select max(r2.readingDate) from MeterReading r2 where r2.meterId = r.meterId)
            """)
    List<MeterReading> findLatest(@Param("meterIds") Collection<Long> meterIds);
}
