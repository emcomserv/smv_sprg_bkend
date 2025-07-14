package com.smartvehicle.repository;

import com.smartvehicle.entity.RouteSchoolStudentMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteSchlStudentMappingRepo extends JpaRepository<RouteSchoolStudentMapping, Integer> {
}
