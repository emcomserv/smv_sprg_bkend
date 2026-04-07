package com.smartvehicle.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class DriverUpdateReq {

    private Long id;
    private String schoolId;
    private String firstName;
    private String lastName;
    private String routeId;
    private String smDriverId;
    private String email;
    private String countryCode;
    private String phone;

}
