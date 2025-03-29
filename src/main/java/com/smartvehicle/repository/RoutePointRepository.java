package com.smartvehicle.repository;


import com.smartvehicle.entity.RoutePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RoutePointRepository extends JpaRepository<RoutePoint, Long> {

    boolean existsBySmRoutePointId(String routePointId);

    Optional<RoutePoint> findBySmRoutePointId(String routePointId);


}

