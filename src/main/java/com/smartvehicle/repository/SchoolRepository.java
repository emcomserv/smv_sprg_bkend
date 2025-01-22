package com.smartvehicle.repository;

import com.smartvehicle.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolRepository extends JpaRepository<School, String> {

    boolean existsById(String id);
    boolean existsByName(String name);
    @Query(
            value = "SELECT s.* FROM smv_school s JOIN smv_admin_reg a ON a.sch_id = s.id " +
                    "WHERE a.id=?1 ",
            nativeQuery = true)
    List<School> findByAdmin_Id(Long adminId);
}
