package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "smv_admin_reg")
@Getter
@Setter
public class Admin extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "sch_id", referencedColumnName = "id")
    private School school;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "sm_admin_id", length = 20)
    private String smAdminId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "smv_admin_route", joinColumns = @JoinColumn(name = "admin_id"), inverseJoinColumns = @JoinColumn(name = "route_id"))
    private List<Route> routes;
}
