package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttenderResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private UserResponseLtDTO user;
    private String phone;
    private Long routeId;
    private String routeName;
    private String smRouteId;
    private String schoolId;
    private String schoolName;
    private String smAttenderId;
}

