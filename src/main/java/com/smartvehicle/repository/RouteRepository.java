package com.smartvehicle.repository;

import com.smartvehicle.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route,Long> {
    Optional<List<Route>> findBySchool_Id(String schoolId);

    Optional<Route> findBySmRouteId(String smRouteId);

    boolean existsByRouteName(String routeName);
    @Query(
            value = "SELECT r.* FROM smv_route r join smv_admin_route ar on ar.route_id = r.id  " +
                    " join smv_admin_reg a on ar.admin_id = a.id  WHERE a.id = ?1  ",
            nativeQuery = true)
    List<Route> findRouteByAdminId(Long adminId);

    @Query(
            value = "SELECT r.* FROM smv_route r join smv_admin_route ar on ar.route_id = r.id  " +
                    " join smv_admin_reg a on ar.admin_id = a.id  WHERE r.sm_route_id = ?1  ",
            nativeQuery = true)
    List<Route> findBySMRoute_Id(String smAdminId );
}
