package com.smart_vehicle.repository;

import com.smart_vehicle.models.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route,String> {
    Optional<List<Route>> findBySchoolUniqueId(String schoolUniqueId);
}
