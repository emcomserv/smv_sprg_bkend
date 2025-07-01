package com.smartvehicle.repository;


import com.smartvehicle.entity.RoutePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface RoutePointRepository extends JpaRepository<RoutePoint, Long> {

    boolean existsBySmRoutePointId(String routePointId);

    Optional<RoutePoint> findBySmRoutePointId(String routePointId);
    List<RoutePoint> findByRoute_Id(Long routeId);
    List<RoutePoint> findByRoute_SmRouteId(String smRouteId);

}

