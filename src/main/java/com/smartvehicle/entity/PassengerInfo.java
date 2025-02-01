package com.smartvehicle.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "smv_passenger_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PassengerInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "sm_route_id")
    private String smRouteId;

    @Column(name = "sm_route_point_id")
    private String smRoutePointId;

    @Column(name = "sm_student_id")
    private String smStudentId;

    @Column(name = "date")
    private String date;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


}
