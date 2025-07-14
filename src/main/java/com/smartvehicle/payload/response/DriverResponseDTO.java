package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private UserResponseLtDTO user;
    private Long routeId;
    private String schoolId;
    private String smDriverId;
}

