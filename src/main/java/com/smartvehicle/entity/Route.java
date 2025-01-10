package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "smv_route")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Route extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "route_name", length = 12)
    private String routeName;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "reserve")
    private Integer reserve;

    @Column(name = "sm_route_id", length = 20)
    private String smRouteId;

    @Column(name = "content", length = 255)
    private String content;
    @OneToMany(mappedBy = "route",fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutePoint> routePoints;
    @ManyToOne
    @JoinColumn(name = "sch_id", referencedColumnName = "id")
    private School school;
}
