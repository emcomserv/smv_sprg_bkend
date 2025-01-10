package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoutePointResponseDTO {
    private Long id;
    private Integer seqOrder;
    private Long routeId;
    private String routePointName;
    private String title;
    private String latitude;
    private String longitude;
    private Boolean status;
    private String reserve;
    private String content;
}

