package com.smartvehicle.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class AdminUpdateReq {

    private Long id;
    private String schoolId;
    private String firstName;
    private String lastName;
    private String email;
    private String countryCode;
    private String phone;
    private String smAdminId;

}
