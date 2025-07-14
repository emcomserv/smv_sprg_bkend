package com.smartvehicle.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverSignupReq extends SignupRequest{

    private Long routeId;
    private String smDriverId;
}
