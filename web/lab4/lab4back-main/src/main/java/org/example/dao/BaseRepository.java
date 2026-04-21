package org.example.dao;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;


public class BaseRepository<T, ID extends Serializable> implements Repository<T, ID> {

    protected final Class<T> eClass;

    public BaseRepository(Class<T> eClass){
        this.eClass=eClass;
    }


    @PersistenceContext(unitName = "my-persistence-unit")
    protected EntityManager entityManager;

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(entityManager.find(eClass, id));
    }

    @Override
    public List<T> findAll() {
        return entityManager.createQuery("SELECT e FROM "+eClass.getSimpleName() + " e", eClass).getResultList();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public T save(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public T update(T entity) {
        return entityManager.merge(entity);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void delete(ID id) {
       findById(id).ifPresent(entityManager::remove);
    }

    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }
}
