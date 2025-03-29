package com.smartvehicle.repository;

import com.smartvehicle.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByUser_Id(Long userId);
    Optional<Driver> findByRoute_Id(Long routeId);
    Optional<Driver> findBySmDriverId(String smDriverId);
}
