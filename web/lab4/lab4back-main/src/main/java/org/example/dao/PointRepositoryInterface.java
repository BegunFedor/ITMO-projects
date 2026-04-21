package org.example.dao;

import org.example.entity.Point;
import org.example.entity.User;

import java.util.List;

public interface PointRepositoryInterface extends Repository<Point, Long>{
    List<Point> findByUserId(Long userId);;
}
