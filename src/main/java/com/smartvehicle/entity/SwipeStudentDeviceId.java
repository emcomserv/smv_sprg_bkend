package com.smartvehicle.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class SwipeStudentDeviceId implements Serializable {
    private String schoolId;
    private String routeId;
    private String studentId;
    private LocalDateTime timestamp;

    // Default constructor
    public SwipeStudentDeviceId() {}

    // All-args constructor (optional, but useful)
    public SwipeStudentDeviceId(String schoolId, String routeId, String studentId, LocalDateTime timestamp) {
        this.schoolId = schoolId;
        this.routeId = routeId;
        this.studentId = studentId;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    // equals and hashCode (required for composite keys)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SwipeStudentDeviceId)) return false;
        SwipeStudentDeviceId that = (SwipeStudentDeviceId) o;
        return Objects.equals(schoolId, that.schoolId) &&
                Objects.equals(routeId, that.routeId) &&
                Objects.equals(studentId, that.studentId) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schoolId, routeId, studentId, timestamp);
    }
}