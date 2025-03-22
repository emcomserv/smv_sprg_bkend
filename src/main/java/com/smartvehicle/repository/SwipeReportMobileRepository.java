package com.smartvehicle.repository;

import com.smartvehicle.entity.SwipeReportMobile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SwipeReportMobileRepository extends JpaRepository<SwipeReportMobile, Long> {
}

