package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "smv_route_school_student_mapping")
public class RouteSchoolStudentMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", referencedColumnName = "id")
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sch_id", referencedColumnName = "id")
    private School school;

    @Column(name = "sm_student_id")
    private String smStudentId;

    @Column(name = "prov_id")
    private String provId;

}
