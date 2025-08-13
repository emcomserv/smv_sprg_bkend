package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "smv_device")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Device extends BaseEntity {
    @Id
    @Column(name = "device_id", nullable = false, length = 20)
    private String deviceId;

    @Column(name = "school_id", nullable = false, length = 20)
    private String schoolId;

    @Column(name = "route_id", nullable = false, length = 20)
    private String routeId;

    @Column(name = "status", nullable = false)
    private Integer status;
}