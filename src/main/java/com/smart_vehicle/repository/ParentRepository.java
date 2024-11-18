package com.smart_vehicle.repository;

import com.smart_vehicle.models.Parent;
import com.smart_vehicle.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParentRepository extends JpaRepository<Parent, String> {

    Parent findByUserId(String userId);
}
