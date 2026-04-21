package org.example.dao;


import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface Repository<T, ID extends Serializable> {

    Optional<T> findById(ID id);
    List<T> findAll();
    T save(T entity);
    T update(T entity);
    void delete(ID id);
    boolean existsById(ID id);

}
