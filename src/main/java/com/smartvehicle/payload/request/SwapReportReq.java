package com.smartvehicle.payload.request;

import lombok.Data;

import java.sql.Date;

@Data
public class SwapReportReq {
    private String studentId;
    private String schoolId;
    private Date startDate;
    private Date endDate;
    private String routeId;
}
