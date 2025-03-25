package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "smv_student_swipe_report")
@Getter
@Setter
@NamedNativeQueries({
        @NamedNativeQuery(name = "SwipeImage.findBySchoolIdAndDateRange", query = "select * from smv_student_swipe_report where sch_id = ?1 and created_date between ?2 and ?3", resultClass = SwipeImage.class),
        @NamedNativeQuery(name = "SwipeImage.findBySchoolAndRouteAndStudentDateRange", query = "select * from smv_student_swipe_report where sch_id = ?1 and route_id = ?2 and student_id = ?3 and created_date between ?4 and ?5", resultClass = SwipeImage.class),
        @NamedNativeQuery(name = "SwipeImage.findBySchoolAndRouteAndDateRange", query = "select * from smv_student_swipe_report where sch_id = ?1 and route_id = ?2 and created_date between ?3 and ?4", resultClass = SwipeImage.class),
        @NamedNativeQuery(name = "SwipeImage.findBySchoolAndStudentAndDateRange", query = "select * from smv_student_swipe_report where sch_id = ?1 and student_id = ?2 and created_date between ?3 and ?4", resultClass = SwipeImage.class),
        @NamedNativeQuery(name = "SwipeImage.findBySchoolAndDateRange", query = "select * from smv_student_swipe_report where sch_id = ?1 and created_date between ?2 and ?3", resultClass = SwipeImage.class)
})
public class SwipeImage {
    @Id
    @Column(name = "image_name", length = 40)
    private String imageName;

    @Column(name = "dev_id", length = 8)
    private String devId;

    @Column(name = "student_id", length = 8)
    private String studentId;

    @Column(name = "sch_id", length = 8)
    private String schId;

    @Column(name = "route_id", length = 8)
    private String routeId;

    @Column(name = "veh_num", length = 12)
    private String vehNum;

    @Column(name = "latitude", length = 10)
    private String latitude;

    @Column(name = "longitude", length = 10)
    private String longitude;

    @Column(name = "date", length = 8)
    private String date;

    @Column(name = "time", length = 8)
    private String time;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "msg_type")
    private Integer msgType;

    @Column(name = "created_date")
    private Date createdDate;
}
