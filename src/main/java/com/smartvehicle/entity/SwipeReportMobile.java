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

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    // Getters and Setters
}
