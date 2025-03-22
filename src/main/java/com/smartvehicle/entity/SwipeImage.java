package com.smartvehicle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "smv_student_swipe_report")
@Getter
@Setter
public class SwipeImage {
    @Id
    @Column(name = "image_name", length = 40)
    private String imageName;

    @Column(name = "dev_id", length = 8)
    private String devId;

    @Column(name = "student_id", length = 8)
    private String studentId;

    @Column(name = "sch_id", length = 8)
    private String schId;

    @Column(name = "route_id", length = 8)
    private String routeId;

    @Column(name = "veh_num", length = 12)
    private String vehNum;

    @Column(name = "latitude", length = 10)
    private String latitude;

    @Column(name = "longitude", length = 10)
    private String longitude;

    @Column(name = "date", length = 8)
    private String date;

    @Column(name = "time", length = 8)
    private String time;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "msg_type")
    private Integer msgType;

}
