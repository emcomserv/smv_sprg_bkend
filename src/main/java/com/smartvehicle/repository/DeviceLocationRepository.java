package com.smartvehicle.repository;

import com.smartvehicle.entity.DeviceLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeviceLocationRepository extends JpaRepository<DeviceLocation, Long> {
    List<DeviceLocation> findBySchoolIdAndRouteIdAndEventTimeBetweenOrderByEventTimeAsc(
            String schoolId,
            String routeId,
            LocalDateTime startInclusive,
            LocalDateTime endInclusive
    );
}


