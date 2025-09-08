package com.smartvehicle.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {
    private Long id;
    private String schoolId;
    private String routeSmId;
    private String driverSmId;
    private String attenderSmId;
    private LocalDate date;
}


