package com.smartvehicle.repository;

import com.smartvehicle.entity.Attender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttenderRepository extends JpaRepository<Attender, Long> {

    Optional<Attender> findByUser_Id(Long userId);
    Optional<Attender> findByRoute_Id(Long routeId);
    Optional<Attender> findBySmAttenderId(String smAttenderId);

}
