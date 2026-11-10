package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itmo.mkd.entity.RequestComment;

import java.util.List;

public interface RequestCommentRepository extends JpaRepository<RequestComment, Long> {

    @Query("""
            select c from RequestComment c
            join fetch c.author
            where c.requestId = :requestId
            order by c.createdAt, c.id
            """)
    List<RequestComment> findByRequest(@Param("requestId") Long requestId);
}
