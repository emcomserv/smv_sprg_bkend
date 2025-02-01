package com.smartvehicle.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RoutePointResponseLtDTO {
    private Long id;
    private Integer seqOrder;
    private String routePointName;
    private String latitude;
    private String longitude;

}

