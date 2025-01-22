package com.smartvehicle.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttenderSignupReq extends SignupRequest{

    private Long routeId;
    private String smAttenderId;
}
