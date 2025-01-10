package com.smartvehicle.repository;

import com.smartvehicle.entity.Driver;
import com.smartvehicle.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Parent findByUser_Id(Long userId);
}
