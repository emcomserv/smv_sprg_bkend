package com.smartvehicle.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "smv_veh_position")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehiclePosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "dev_id", length = 12)
    private String devId;

    @Column(name = "route_id", length = 20)
    private String routeId;

    @Column(name = "veh_num", length = 14)
    private String vehNum;

    @Column(name = "latitude", length = 12)
    private String latitude;

    @Column(name = "longitude", length = 12)
    private String longitude;

    @Column(name = "date", length = 10)
    private String date;

    @Column(name = "time", length = 10)
    private String time;

    @Column(name = "status")
    private Integer status;

    @Column(name = "msg_type")
    private Integer msgType;

    @Column(name = "reserve", length = 8)
    private String reserve;

    @Column(name = "content", length = 255)
    private String content;

    @Column(name = "title", length = 255)
    private String title;
}
