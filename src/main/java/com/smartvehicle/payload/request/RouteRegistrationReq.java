package com.smartvehicle.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteRegistrationReq {

    @NotBlank(message = "Route name is required")
    private String routeName;

    private String title;

    private Boolean status;

    private Integer reserve;

    @NotBlank(message = "SM Route ID is required")
    private String smRouteId;

    private String content;

    @NotNull(message = "School ID is required")
    private String schoolId;
}
