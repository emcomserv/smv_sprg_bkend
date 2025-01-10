package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String schId;
    private Long parentId;
    private Integer age;
    private String gender;
    private Boolean status;
    private Long routeId;
}

