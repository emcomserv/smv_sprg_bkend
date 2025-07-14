package com.smartvehicle.repository;

import com.smartvehicle.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {

    Parent findByUser_Id(Long userId);

    Optional<Parent> findBySmParentId(String smParentId);
    List<Parent> findBySchool_Id(String schoolId);
}
