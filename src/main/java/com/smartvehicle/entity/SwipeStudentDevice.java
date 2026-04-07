package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@IdClass(SwipeStudentDeviceId.class)
@Table(name = "smv_swipe_student_device")
public class SwipeStudentDevice {

    @Id
    @Column(name = "school_id", nullable = false)
    private String schoolId;

    @Id
    @Column(name = "route_id", nullable = false)
    private String routeId;

    @Id
    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Id
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "latitude", nullable = false)
    private String latitude;

    @Column(name = "longitude", nullable = false)
    private String longitude;

    @Column(name = "imagename")
    private String imageName;

    @Column(name = "reserv")
    private String reserv;
} 