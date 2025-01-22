package com.smartvehicle.repository;

import com.smartvehicle.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
