package com.smartvehicle.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class RouteRegResDTO {
    private Long routeId;
    private String routeName;
    private String title;
    private Boolean status;
    private String schoolName;
}