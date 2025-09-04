package com.smartvehicle.repository;

import com.smartvehicle.entity.SwipeStudentDevice;
import com.smartvehicle.entity.SwipeStudentDeviceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SwipeStudentDeviceRepository extends JpaRepository<SwipeStudentDevice, SwipeStudentDeviceId> {
    // Returns the latest swipe record for a student by timestamp
    SwipeStudentDevice findTopByStudentIdOrderByTimestampDesc(String studentId);

    // Distinct student IDs within date range for a school+route
    @Query("select distinct s.studentId from SwipeStudentDevice s where s.schoolId = :schoolId and s.routeId = :routeId and s.timestamp between :start and :end order by s.studentId")
    List<String> findDistinctStudentIdsBySchoolRouteAndDateRange(
            @Param("schoolId") String schoolId,
            @Param("routeId") String routeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Latest swipe rows per student within date range for a school+route
    @Query("select s from SwipeStudentDevice s where s.schoolId = :schoolId and s.routeId = :routeId and s.timestamp between :start and :end and s.timestamp = (select max(s2.timestamp) from SwipeStudentDevice s2 where s2.studentId = s.studentId and s2.schoolId = :schoolId and s2.routeId = :routeId and s2.timestamp between :start and :end)")
    List<SwipeStudentDevice> findLatestSwipesPerStudentBySchoolRouteAndDateRange(
            @Param("schoolId") String schoolId,
            @Param("routeId") String routeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // All swipe rows (including duplicates for the same student) in time range, ordered by timestamp
    List<SwipeStudentDevice> findBySchoolIdAndRouteIdAndTimestampBetweenOrderByTimestampAsc(
            String schoolId,
            String routeId,
            LocalDateTime start,
            LocalDateTime end);

    // All swipe rows by school only in time range, ordered by timestamp
    List<SwipeStudentDevice> findBySchoolIdAndTimestampBetweenOrderByTimestampAsc(
            String schoolId,
            LocalDateTime start,
            LocalDateTime end);

    // All swipe rows by school, ordered by timestamp (no date filter)
    List<SwipeStudentDevice> findBySchoolIdOrderByTimestampAsc(String schoolId);
} 