package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchoolResponseDTO {
    private String id;
    private String name;
    private String countryId;
    private String provId;
    private String areaId;
    private String entityId;
    private String contactNum;
    private String contactName;
    private Boolean status;
}

