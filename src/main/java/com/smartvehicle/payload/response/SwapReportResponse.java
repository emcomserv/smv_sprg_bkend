package com.smartvehicle.payload.response;

import lombok.Data;

@Data
public class SwapReportResponse {
    private String studentId;
    private String schoolId;
    private String routeId;
    private String studentName;
    private String latitude;
    private String longitude;
    private String imageName;
}
