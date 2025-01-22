package com.smartvehicle.repository;

import com.smartvehicle.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public  interface AdminRepository extends JpaRepository<Admin,Long> {
    Optional<Admin> findByUser_Id(Long userId);

    @Query(
            value = "SELECT a.* FROM smv_admin_reg a join smv_admin_route ar on ar.admin_id=a.id " +
                    " join smv_route r on ar.route_id = r.id  WHERE r.id = ?1  ",
            nativeQuery = true)
    List<Admin> findByRoute_Id(Long routeId);

    @Query(
            value = "SELECT a.* FROM smv_admin_reg a join smv_admin_route ar on ar.admin_id=a.id " +
                    " join smv_route r on ar.route_id = r.id  WHERE r.sm_route_id = ?1  ",
            nativeQuery = true)
    List<Admin> findBySMRoute_Id(String smRouteId );

    List<Admin> findBySchool_Id(String schoolId);
}