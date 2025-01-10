package com.smartvehicle.repository;

import com.smartvehicle.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route,Long> {
    Optional<List<Route>> findBySchool_Id(String schoolId);
}
