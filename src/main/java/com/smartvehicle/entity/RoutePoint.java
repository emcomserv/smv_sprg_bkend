package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "smv_route_point")
public class RoutePoint extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seq_order", nullable = false)
    private Integer seqOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "route_point_name")
    private String routePointName;

    @Column(name = "title")
    private String title;

    @Column(name = "latitude")
    private String latitude;

    @Column(name = "longitude")
    private String longitude;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "reserve")
    private String reserve;

    @Column(name = "content")
    private String content;


}
