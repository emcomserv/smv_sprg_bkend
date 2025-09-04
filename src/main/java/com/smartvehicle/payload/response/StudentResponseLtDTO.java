package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentResponseLtDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private Long parentId;
    private String parentFirstName;
    private String parentLastName;
    private Long routeId;
    private String schoolId;
    private String routeName;
    private String schoolName;
    private String smStudentId;

}

