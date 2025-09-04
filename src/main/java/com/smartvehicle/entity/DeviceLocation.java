package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "smv_device_location")
@Getter
@Setter
public class  DeviceLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", length = 20, nullable = false)
    private String deviceId;

    @Column(name = "school_id", length = 20, nullable = false)
    private String schoolId;

    @Column(name = "route_id", length = 20, nullable = false)
    private String routeId;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;
}


