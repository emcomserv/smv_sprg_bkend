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

    // Find students by external route id (smRouteId)
    List<Student> findAllByRoute_SmRouteId(String smRouteId);

    List<Student> findAllByRoutePoint_Id(Long id);

    List<Student> findBySchool_Id(String schoolId);

    Optional<Student> findBySmStudentId(String smStudentId);

    @Query("SELECT s FROM Student s WHERE s.smStudentId = :studentId")
    Student findByRouteSchoolAndStudentId( @Param("studentId") String studentId);

    // Fetch parent's FCM token directly without triggering lazy loading from background thread
    @Query("SELECT u.deviceToken FROM Student s JOIN s.parent p JOIN p.user u WHERE s.smStudentId = :smStudentId")
    Optional<String> findParentDeviceTokenBySmStudentId(@Param("smStudentId") String smStudentId);
    
    // Get student sm_student_id list by schoolId and smRouteId
    @Query("SELECT s.smStudentId FROM Student s WHERE s.school.id = :schoolId AND s.route.id IN (SELECT r.id FROM Route r WHERE r.smRouteId = :smRouteId)")
    List<String> findSmStudentIdsBySchoolIdAndSmRouteId(@Param("schoolId") String schoolId,
                                                        @Param("smRouteId") String smRouteId);
    //@Query("SELECT s FROM Student s WHERE s.route.id = :routeId AND s.school.id = :schoolId AND s.smStudentId = :studentId")
    //Student findByRouteSchoolAndStudentId(@Param("routeId") Long routeId, 
    //                                      @Param("schoolId") Long schoolId, 
    //                                      @Param("studentId") String studentId);
}
