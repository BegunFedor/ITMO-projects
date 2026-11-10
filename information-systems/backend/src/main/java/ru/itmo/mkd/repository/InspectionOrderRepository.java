package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.itmo.mkd.entity.InspectionOrder;

import java.util.List;

public interface InspectionOrderRepository extends JpaRepository<InspectionOrder, Long> {

    @Query("""
            select distinct o from InspectionOrder o
            join fetch o.company
            left join fetch o.requests
            where o.inspector.id = :inspectorId
            order by o.issuedAt desc
            """)
    List<InspectionOrder> findByInspector(@Param("inspectorId") Long inspectorId);

    @Query("""
            select distinct o from InspectionOrder o
            join fetch o.company
            left join fetch o.requests
            where o.company.id = :companyId
            order by o.issuedAt desc
            """)
    List<InspectionOrder> findByCompany(@Param("companyId") Long companyId);
}
