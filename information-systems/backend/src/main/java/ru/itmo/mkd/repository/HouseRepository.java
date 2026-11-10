package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.mkd.entity.House;

import java.util.List;

public interface HouseRepository extends JpaRepository<House, Long> {

    List<House> findAllByOrderByAddress();

    List<House> findByCompanyIdOrderByAddress(Long companyId);
}
