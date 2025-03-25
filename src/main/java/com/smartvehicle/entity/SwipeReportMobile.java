package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "smv_swipe_student_mobile")
@NamedNativeQueries({
        @NamedNativeQuery(name = "SwipeReportMobile.findBySchoolIdAndDateRange", query = "select * from smv_swipe_student_mobile where school_id = ?1 and timestamp between ?2 and ?3", resultClass = SwipeReportMobile.class),
        @NamedNativeQuery(name = "SwipeReportMobile.findBySchoolAndRouteAndStudentDateRange", query = "select * from smv_swipe_student_mobile where school_id = ?1 and route_id = ?2 and student_id = ?3 and timestamp between ?4 and ?5", resultClass = SwipeReportMobile.class),
        @NamedNativeQuery(name = "SwipeReportMobile.findBySchoolAndRouteAndDateRange", query = "select * from smv_swipe_student_mobile where school_id = ?1 and route_id = ?2 and timestamp between ?3 and ?4", resultClass = SwipeReportMobile.class),
        @NamedNativeQuery(name = "SwipeReportMobile.findBySchoolAndStudentAndDateRange", query = "select * from smv_swipe_student_mobile where school_id = ?1 and student_id = ?2 and timestamp between ?3 and ?4", resultClass = SwipeReportMobile.class),
        @NamedNativeQuery(name = "SwipeReportMobile.findBySchoolAndDateRange", query = "select * from smv_swipe_student_mobile where school_id = ?1 and timestamp between ?2 and ?3", resultClass = SwipeReportMobile.class)
})
public class SwipeReportMobile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "route_id",nullable = false)
    private String routeId;

    @Column(name = "school_id",nullable = false)
    private String schoolId;

    @Column(name = "student_id",nullable = false)
    private String studentId;

    @Column(name = "latitude",nullable = false)
    private String latitude;

    @Column(name = "longitude",nullable = false)
    private String longitude;

    @Column(name = "image_name",nullable = false)
    private String imageName;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

}
