package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.mkd.entity.RequestCategory;

import java.util.List;

public interface RequestCategoryRepository extends JpaRepository<RequestCategory, Long> {

    List<RequestCategory> findByActiveTrueOrderByName();
}
