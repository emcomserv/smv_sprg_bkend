package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "smv_student")
@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class Student extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "first_name",  length = 100)
    private String firstName;

    @Column(name = "last_name",  length = 100)
    private String lastName;

    @Column(name = "age", length = 20)
    private String age;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "status")
    private Boolean status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", referencedColumnName = "id")
    private Route route;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sch_id", referencedColumnName = "id")
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private Parent parent;

    @Column(name = "sm_student_id", length = 20)
    private String smStudentId;

    @Column(name = "latitude")
    private String latitude;

    @Column(name = "longitude")
    private String longitude;
    @OneToOne
    @JoinColumn(name = "route_point_id")
    private RoutePoint routePoint;

    @Column(name = "device_id", length = 100)
    private String deviceId;
}
