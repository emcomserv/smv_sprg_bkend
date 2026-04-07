package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String schId;
    private String schoolName;
    private UserResponseDTO user;
    private String smAdminId;
    private List<RouteResponseLtDTO> routes;
}

