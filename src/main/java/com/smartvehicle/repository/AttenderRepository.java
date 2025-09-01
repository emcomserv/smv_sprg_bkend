package com.smartvehicle.repository;

import com.smartvehicle.entity.Attender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttenderRepository extends JpaRepository<Attender, Long> {

    Optional<Attender> findByUser_Id(Long userId);
    Optional<Attender> findByRoute_Id(Long routeId);
    Optional<Attender> findBySmAttenderId(String smAttenderId);
    /**
     * Find attender by ID with all relationships loaded
     */
    @Query("SELECT a FROM Attender a " +
            "LEFT JOIN FETCH a.route " +
            "LEFT JOIN FETCH a.school " +
            "LEFT JOIN FETCH a.user " +
            "WHERE a.id = :id")
    Optional<Attender> findByIdWithRelationships(@Param("id") Long id);

    /**
     * Find all attenders with all relationships loaded
     */
    @Query("SELECT a FROM Attender a " +
            "LEFT JOIN FETCH a.route " +
            "LEFT JOIN FETCH a.school " +
            "LEFT JOIN FETCH a.user")
    List<Attender> findAllWithRelationships();

    List<Attender> findBySchool_IdIn(List<String> schoolIds);
}
