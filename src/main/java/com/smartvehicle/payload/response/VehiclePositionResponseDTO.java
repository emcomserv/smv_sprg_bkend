package com.smartvehicle.payload.response;


import lombok.Data;

@Data
public class VehiclePositionResponseDTO {
    private Long id;
    private String devId;
    private String routeId;
    private String vehNum;
    private String latitude;
    private String longitude;
    private String date;
    private String time;
    private Integer status;
    private Integer msgType;
    private String reserve;
    private String content;
    private String title;
    private String smPrevRoutePointId;
}

