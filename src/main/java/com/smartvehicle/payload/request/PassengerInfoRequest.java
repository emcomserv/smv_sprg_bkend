package com.smartvehicle.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PassengerInfoRequest {

    private String SmRouteId;
    private String SmRoutePointId;
    private String SmStudentId;
}
