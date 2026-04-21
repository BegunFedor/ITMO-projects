package org.example.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.entity.Point;
import org.example.entity.User;

import java.math.BigDecimal;
import java.util.List;

@Stateless
@LocalBean
public class PointRepository extends BaseRepository<Point, Long> implements PointRepositoryInterface {

    @PersistenceContext
    protected EntityManager entityManager;

    public PointRepository() {
        super(Point.class);
    }


    public List<Point> findByUserId(Long userId) {
        return entityManager.createQuery(
                        "SELECT p FROM Point p " +
                                "WHERE p.user.id = :userId " +   // сравниваем по id
                                "ORDER BY p.currentTime DESC",
                        Point.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Point> findPointsInBBox(Long userId,
                                        double minX, double minY,
                                        double maxX, double maxY) {

        return entityManager.createQuery(
                        "SELECT p FROM Point p " +
                                "WHERE p.user.id = :userId " +
                                "AND p.x BETWEEN :minX AND :maxX " +
                                "AND p.y BETWEEN :minY AND :maxY " +
                                "ORDER BY p.currentTime DESC",
                        Point.class)
                .setParameter("userId", userId)
                .setParameter("minX", BigDecimal.valueOf(minX))
                .setParameter("maxX", BigDecimal.valueOf(maxX))
                .setParameter("minY", BigDecimal.valueOf(minY))
                .setParameter("maxY", BigDecimal.valueOf(maxY))
                .getResultList();
    }

    public void updateCoords(Long pointId, double x, double y) {
        entityManager.createQuery(
                        "UPDATE Point p SET p.x = :x, p.y = :y WHERE p.id = :id")
                .setParameter("x", BigDecimal.valueOf(x))
                .setParameter("y", BigDecimal.valueOf(y))
                .setParameter("id", pointId)
                .executeUpdate();
    }
}