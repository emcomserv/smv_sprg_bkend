package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String schId;
    private Long userId;
}

