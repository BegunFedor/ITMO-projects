package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.mkd.entity.Rating;

public interface RatingRepository extends JpaRepository<Rating, Long> {
}
