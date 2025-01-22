package com.smartvehicle.repository;

import com.smartvehicle.entity.Route;
import com.smartvehicle.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByParent_Id(Long parentId);

    List<Student> findByRoute_Id(Long routeId);
}
