package org.example.dao;

import org.example.entity.Area;
import org.example.entity.User;

import java.util.List;
import java.util.Optional;

public interface AreaRepositoryInterface extends Repository<Area, Long> {

    List<Area> findByUser(User user);

    Optional<Area> findByIdAndUser(Long id, User user);

    void deleteByIdAndUser(Long id, User user);
}
