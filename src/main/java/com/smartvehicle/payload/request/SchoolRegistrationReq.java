package com.smartvehicle.payload.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchoolRegistrationReq {

    @NotBlank(message = "School ID is required")
    private String schoolId;
    @NotBlank(message = "School name is required")
    private String name;

    private String countryId;

    private String provId;

    private String areaId;

    private String entityId;

    @NotBlank(message = "Contact name is required")
    private String contactName;

    @NotBlank(message = "Contact number is required")
    private String contactNum;

    private Boolean status;
}

