package com.smartvehicle.repository;

import com.smartvehicle.entity.SwipeImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface SwipeImageRepository extends JpaRepository<SwipeImage, String> {
    List<SwipeImage> findByStudentId(String student);

    List<SwipeImage> findBySchoolIdAndDateRange(String schoolId, Date startDate, Date endDate);

    List<SwipeImage> findBySchoolAndRouteAndStudentDateRange(String schoolId, String routeId, String studentId, Date startDate, Date endDate);

    List<SwipeImage> findBySchoolAndRouteAndDateRange(String schoolId, String routeId, Date startDate, Date endDate);

    List<SwipeImage> findBySchoolAndStudentAndDateRange(String schoolId, String studentId, Date startDate, Date endDate);

    List<SwipeImage> findBySchoolAndDateRange(String schoolId, Date startDate, Date endDate);
}
