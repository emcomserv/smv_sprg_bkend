package com.smartvehicle.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RecognitionResultRequest {

    @NotBlank
    private String input; // SCHOOLID-ROUTEID-CITYCODESTUDENTID (e.g., AC0F0001-RT0F0004-BNGST0F0001)
    
    private String schoolId; // derived from input
    private String routeId;  // derived from input
    private String cityCode; // derived from input
    private String studentId; // derived from input

    @NotBlank
    private String latitude;

    @NotBlank
    private String longitude;

    @NotBlank
    private String result; // SUCCESS | FAIL

    @NotNull
    private Double confidence; // 0-100

    private String imageBase64; // optional

    private String reason; // optional (e.g., FACE_NOT_MATCHED, LOW_LIGHT)

    // getters for derived fields
    public String getSchoolId() { return schoolId; }
    public void setSchoolId(String schoolId) { this.schoolId = schoolId; }
    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }
    public String getCityCode() { return cityCode; }
    public void setCityCode(String cityCode) { this.cityCode = cityCode; }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    // end derived fields
}


