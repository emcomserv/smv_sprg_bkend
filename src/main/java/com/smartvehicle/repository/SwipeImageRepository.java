package com.smartvehicle.repository;

import com.smartvehicle.entity.Student;
import com.smartvehicle.entity.SwipeImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SwipeImageRepository extends JpaRepository<SwipeImage, String> {
    List<SwipeImage> findByStudentId(String student);
}
