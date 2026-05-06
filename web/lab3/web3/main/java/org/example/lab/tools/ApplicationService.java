package org.example.lab.tools;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.example.lab.entities.PointEntity;

import java.util.List;

@ApplicationScoped
public class ApplicationService {

    @PersistenceContext(unitName = "labPU")
    private EntityManager entityManager;

    @Transactional
    public void savePoint(PointEntity point) {
        entityManager.persist(point);
    }

    public List<PointEntity> getAllPoints() {
        return entityManager.createQuery(
                "SELECT p FROM PointEntity p ORDER BY p.timestamp DESC",
                PointEntity.class
        ).getResultList();
    }
}