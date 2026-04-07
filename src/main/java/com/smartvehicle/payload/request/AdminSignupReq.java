package com.smartvehicle.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSignupReq extends SignupRequest{
    private String smAdminId;
    private Long routeId;
}
