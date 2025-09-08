package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "smv_assignment")
public class Assignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", referencedColumnName = "id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attender_id", referencedColumnName = "id")
    private Attender attender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", referencedColumnName = "id")
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sch_id", referencedColumnName = "id")
    private School school;

    @Column(name = "assignment_date", nullable = false)
    private LocalDate assignmentDate;
}


