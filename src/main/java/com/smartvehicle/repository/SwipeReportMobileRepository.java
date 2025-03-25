package com.smartvehicle.repository;

import com.smartvehicle.entity.SwipeReportMobile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

@Repository
public interface SwipeReportMobileRepository extends JpaRepository<SwipeReportMobile, Long> {

    List<SwipeReportMobile> findBySchoolIdAndDateRange(String schoolId, Timestamp startDate, Timestamp endDate);

    List<SwipeReportMobile> findBySchoolAndRouteAndStudentDateRange(String schoolId, String routeId, String studentId, Timestamp startDate, Timestamp endDate);

    List<SwipeReportMobile> findBySchoolAndRouteAndDateRange(String schoolId, String routeId, Timestamp startDate, Timestamp endDate);

    List<SwipeReportMobile> findBySchoolAndStudentAndDateRange(String schoolId, String studentId, Timestamp startDate, Timestamp endDate);

    List<SwipeReportMobile> findBySchoolAndDateRange(String schoolId, Timestamp startDate, Timestamp endDate);
}

