package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itmo.mkd.entity.RequestStatusHistory;

import java.util.List;

public interface RequestStatusHistoryRepository extends JpaRepository<RequestStatusHistory, Long> {

    @Query("""
            select h from RequestStatusHistory h
            left join fetch h.changedBy
            where h.requestId = :requestId
            order by h.id
            """)
    List<RequestStatusHistory> findByRequest(@Param("requestId") Long requestId);
}
