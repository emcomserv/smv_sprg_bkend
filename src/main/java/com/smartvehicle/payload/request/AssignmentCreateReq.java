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
    private String smRouteId; // sm route id

    private String smDriverID; // optional

    private String smAttenderId; // optional

    @NotNull
    private LocalDate date; // assignment date

    @NotNull
    private Integer status; // assignment status
}


