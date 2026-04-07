package com.smartvehicle.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoutePointRegistrationReq {

    @NotNull(message = "Sequence order is required")
    private Integer seqOrder;

    @NotNull(message = "Route ID is required")
    private Long routeId;

    @NotBlank(message = "Route point name is required")
    private String routePointName;

    private String title;

    private String latitude;

    private String longitude;

    private Boolean status;
    private String reserve;
    private String content;
    private String schoolId;
    private String smRoutePointId;


}
