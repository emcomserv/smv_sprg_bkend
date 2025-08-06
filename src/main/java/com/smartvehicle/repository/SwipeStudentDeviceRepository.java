package com.smartvehicle.repository;

import com.smartvehicle.entity.SwipeStudentDevice;
import com.smartvehicle.entity.SwipeStudentDeviceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SwipeStudentDeviceRepository extends JpaRepository<SwipeStudentDevice, SwipeStudentDeviceId> {
    // Returns the latest swipe record for a student by timestamp
    SwipeStudentDevice findTopByStudentIdOrderByTimestampDesc(String studentId);
} 