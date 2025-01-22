package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RouteResponseLtDTO {
    private Long id;
    private String schId;
    private String routeName;
    private String smRouteId;
}
