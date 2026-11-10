package ru.itmo.mkd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.mkd.entity.Apartment;

import java.util.List;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {

    List<Apartment> findByHouseIdOrderById(Long houseId);
}
