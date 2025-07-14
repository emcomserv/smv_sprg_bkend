package com.smartvehicle.repository;

import com.smartvehicle.entity.PassengerInfo;
import com.smartvehicle.payload.response.PassengerInfoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PassengerInfoRepository extends JpaRepository<PassengerInfo, Long> {
    @Query(
            value = "SELECT ps.sm_route_id as smRouteId,ps.sm_route_point_id as smRoutePointId," +
                    " ps.sm_student_id as smStudentId, r.id as routeId, r.route_name as routeName," +
                    " rp.id as routePointId, rp.route_point_name as routePointName, " +
                    " s.id as studentId,s.first_name as studentFirstName " +
                    " FROM smv_passenger_info ps JOIN smv_route r ON r.sm_route_id = ps.sm_route_id " +
                    "    JOIN smv_route_point rp ON rp.sm_route_point_id= ps.sm_route_point_id " +
                    "    JOIN smv_student s ON s.sm_student_id = ps.sm_student_id  ",
            nativeQuery = true)
    List<PassengerInfoResponse> fetchPassengerInfo();
    @Query(
            value = "SELECT ps.sm_route_id as smRouteId,ps.sm_route_point_id as smRoutePointId," +
                    " ps.sm_student_id as smStudentId, r.id as routeId, r.route_name as routeName," +
                    " rp.id as routePointId, rp.route_point_name as routePointName, " +
                    " s.id as studentId,s.first_name as studentFirstName " +
                    " FROM smv_passenger_info ps JOIN smv_route r ON r.sm_route_id = ps.sm_route_id " +
                    "    JOIN smv_route_point rp ON rp.sm_route_point_id= ps.sm_route_point_id " +
                    "    JOIN smv_student s ON s.sm_student_id = ps.sm_student_id  " +
                    " WHERE rp.sm_route_point_id =?1 ",
            nativeQuery = true)
    List<PassengerInfoResponse> fetchPassengerInfo(String smRoutePointId);

//    @Query(value = "SELECT EXISTS (SELECT 1 FROM smv_passenger_info " +
//            "WHERE sm_route_point_id = :smRoutePointId AND sm_student_id = :smStudentId)", nativeQuery = true)
//    boolean existsBySmRoutePointAndSmStudent(@Param("smRoutePointId") String smRoutePointId,
//                                    @Param("smStudentId") String smStudentId);

    boolean existsBySmRoutePointIdAndSmStudentId(String smRoutePointId ,String smStudentId);
}
