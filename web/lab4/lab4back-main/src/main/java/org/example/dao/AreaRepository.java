package org.example.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.example.entity.Area;
import org.example.entity.User;

import java.util.List;
import java.util.Optional;

@Stateless
@LocalBean
public class AreaRepository extends BaseRepository<Area, Long>
        implements AreaRepositoryInterface {

    public AreaRepository() {
        super(Area.class);
    }

    @Override
    public List<Area> findByUser(User user) {
        return entityManager.createQuery(
                        "SELECT a FROM Area a " +
                                "WHERE a.user = :user " +
                                "ORDER BY a.createdAt DESC",
                        Area.class)
                .setParameter("user", user)
                .getResultList();
    }

    @Override
    public Optional<Area> findByIdAndUser(Long id, User user) {
        return entityManager.createQuery(
                        "SELECT a FROM Area a " +
                                "WHERE a.id = :id AND a.user = :user",
                        Area.class)
                .setParameter("id", id)
                .setParameter("user", user)
                .getResultStream()
                .findFirst();
    }

    @Override
    public void deleteByIdAndUser(Long id, User user) {
        entityManager.createQuery(
                        "DELETE FROM Area a WHERE a.id = :id AND a.user = :user")
                .setParameter("id", id)
                .setParameter("user", user)
                .executeUpdate();
    }
}