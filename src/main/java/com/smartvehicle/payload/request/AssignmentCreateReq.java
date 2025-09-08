package com.smartvehicle.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AssignmentCreateReq {

    @NotBlank
    private String schoolId; // sm school id

    @NotBlank
    private String routeSmId; // sm route id

    private String driverSmId; // optional

    private String attenderSmId; // optional

    @NotNull
    private LocalDate date; // assignment date
}


