package com.smartvehicle.repository;

import com.smartvehicle.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    Optional<Assignment> findBySchool_IdAndRoute_IdAndAssignmentDate(String schoolId, Long routeId, LocalDate assignmentDate);

    List<Assignment> findBySchool_IdAndAssignmentDate(String schoolId, LocalDate assignmentDate);

    List<Assignment> findByRoute_IdAndAssignmentDate(Long routeId, LocalDate assignmentDate);

    @Query("select a from Assignment a where a.school.id = ?1 and a.route.smRouteId = ?2 and a.assignmentDate = ?3")
    Optional<Assignment> findBySchoolIdAndSmRouteIdAndDate(String schoolId, String smRouteId, LocalDate date);

    @Query("select a from Assignment a where a.school.id = ?1 and a.route.smRouteId = ?2 and a.assignmentDate = ?3")
    List<Assignment> findAllBySchoolIdAndSmRouteIdAndDate(String schoolId, String smRouteId, LocalDate date);

    Optional<Assignment> findByDriver_IdAndAssignmentDate(Long driverId, LocalDate assignmentDate);

    Optional<Assignment> findByAttender_IdAndAssignmentDate(Long attenderId, LocalDate assignmentDate);

    // Find active assignments for a given school and date
    @Query("select a from Assignment a where a.school.id = ?1 and a.assignmentDate <= ?2 and (a.endDate is null or a.endDate >= ?2)")
    List<Assignment> findActiveBySchoolAndDate(String schoolId, LocalDate date);

    // Find overlapping assignments for a driver on a date range
    @Query("select a from Assignment a where a.driver.id = ?1 and a.assignmentDate <= ?3 and (a.endDate is null or a.endDate >= ?2)")
    List<Assignment> findDriverOverlaps(Long driverId, LocalDate start, LocalDate end);

    // Find overlapping assignments for an attender on a date range
    @Query("select a from Assignment a where a.attender.id = ?1 and a.assignmentDate <= ?3 and (a.endDate is null or a.endDate >= ?2)")
    List<Assignment> findAttenderOverlaps(Long attenderId, LocalDate start, LocalDate end);
}


