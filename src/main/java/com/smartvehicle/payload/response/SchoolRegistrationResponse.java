package com.smartvehicle.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SchoolRegistrationResponse {
    private String schoolId;
    private String name;
    private String contactName;
    private String contactNum;
}
