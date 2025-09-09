package com.smartvehicle.payload.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AssignmentUpdateReq {
    private String routeSmId; // optional
    private String driverSmId; // optional
    private String attenderSmId; // optional
    private LocalDate date; // optional new start date
    private LocalDate endDate; // optional explicit end date
}


