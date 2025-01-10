package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DriverResponseDTO {
    private Long id;
    private String schId;
    private String firstName;
    private String lastName;
    private Long userId;
}

