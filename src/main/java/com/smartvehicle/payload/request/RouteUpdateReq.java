package com.smartvehicle.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteUpdateReq {

    private Long id;
    private String routeName;
    private String title;
    private Boolean status;
    private Integer reserve;
    private String smRouteId;
    private String content;
    private String schoolId;

}
