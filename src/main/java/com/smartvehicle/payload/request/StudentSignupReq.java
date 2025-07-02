package com.smartvehicle.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentSignupReq {
    private Long parentId;
    private String age;
    private String gender;
    private Boolean status;
    private Long routeId;
    private String smStudentId;
    private String latitude;
    private String longitude;
    private Long routePointId;
    private String firstName;
    private String lastName;
    private String schoolId;
}