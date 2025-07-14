package com.smartvehicle.repository;

import com.smartvehicle.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findAllByParent_Id(Long parentId);

    List<Student> findAllByRoute_Id(Long routeId);

    List<Student> findAllByRoutePoint_Id(Long id);

    List<Student> findBySchool_Id(String schoolId);

    Optional<Student> findBySmStudentId(String smStudentId);

    @Query("SELECT s FROM Student s WHERE s.smStudentId = :studentId")
    Student findByRouteSchoolAndStudentId( @Param("studentId") String studentId);
    //@Query("SELECT s FROM Student s WHERE s.route.id = :routeId AND s.school.id = :schoolId AND s.smStudentId = :studentId")
    //Student findByRouteSchoolAndStudentId(@Param("routeId") Long routeId, 
    //                                      @Param("schoolId") Long schoolId, 
    //                                      @Param("studentId") String studentId);
}
