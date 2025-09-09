package com.smartvehicle.payload.response;

import java.time.LocalDateTime;

public class SwipeStudentSummaryDTO {
    private String schoolId;
    private String routeId;
    private String studentId;
    private String latitude;
    private String longitude;
    private LocalDateTime timestamp;
    private String reserv;
    private String routePointName;
    private String source; // "device" or "mobile"

    public SwipeStudentSummaryDTO() {}

    public SwipeStudentSummaryDTO(String schoolId, String routeId, String studentId,
                                  String latitude, String longitude, LocalDateTime timestamp, String reserv,
                                  String routePointName, String source) {
        this.schoolId = schoolId;
        this.routeId = routeId;
        this.studentId = studentId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.reserv = reserv;
        this.routePointName = routePointName;
        this.source = source;
    }

    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }
    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getReserv() { return reserv; }
    public void setReserv(String reserv) { this.reserv = reserv; }
    public String getRoutePointName() { return routePointName; }
    public void setRoutePointName(String routePointName) { this.routePointName = routePointName; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}


