package com.smartvehicle.payload.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AssignmentUpdateReq {
    private String smRouteId; // optional
    private String smDriverID; // optional
    private String smAttenderId; // optional
    private LocalDate date; // optional new start date
    private LocalDate endDate; // optional explicit end date
    private Integer status; // optional status update
}


