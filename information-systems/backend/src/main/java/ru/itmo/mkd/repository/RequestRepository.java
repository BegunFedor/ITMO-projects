package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itmo.mkd.domain.RequestStatus;
import ru.itmo.mkd.entity.Request;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    @Query("""
            select r from Request r
            join fetch r.category
            join fetch r.house
            left join fetch r.apartment
            left join fetch r.executor
            where r.author.id = :authorId
            order by r.createdAt desc
            """)
    List<Request> findByAuthor(@Param("authorId") Long authorId);

    @Query("""
            select r from Request r
            join fetch r.category
            join fetch r.house h
            left join fetch r.apartment
            left join fetch r.executor
            where h.company.id = :companyId and r.status in :statuses
            order by r.overdue desc, r.createdAt desc
            """)
    List<Request> findByCompanyAndStatuses(@Param("companyId") Long companyId,
                                           @Param("statuses") Collection<RequestStatus> statuses);

    @Query("""
            select r from Request r
            join fetch r.category
            join fetch r.house
            left join fetch r.apartment
            left join fetch r.executor
            where r.executor.id = :executorId and r.status in :statuses
            order by r.deadlineAt
            """)
    List<Request> findByExecutorAndStatuses(@Param("executorId") Long executorId,
                                            @Param("statuses") Collection<RequestStatus> statuses);

    @Query("""
            select r from Request r
            join fetch r.author
            join fetch r.category
            join fetch r.house h
            join fetch h.company
            join fetch h.district
            left join fetch r.apartment
            left join fetch r.executor
            where r.id = :id
            """)
    Optional<Request> findCard(@Param("id") Long id);
}
