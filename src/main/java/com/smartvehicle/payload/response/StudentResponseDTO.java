package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private Long parentId;
    private Integer age;
    private String gender;
    private Boolean status;
    private Long routeId;
    private String schoolId;
    private String routeName;
    private String schoolName;
    private String smStudentId;
    private String latitude;

    private String longitude;
}

