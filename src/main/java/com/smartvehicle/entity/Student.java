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

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "age", length = 20)
    private String age;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "status")
    private Boolean status;

    @ManyToOne
    @JoinColumn(name = "route_id", referencedColumnName = "id")
    private Route route;
    @ManyToOne
    @JoinColumn(name = "sch_id", referencedColumnName = "id")
    private School school;

    @ManyToOne
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private Parent parent;

}
