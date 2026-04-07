package com.smartvehicle.payload.response;

import java.util.List;

public class UploadResponseDTO {
    private boolean success;
    private String message;
    private List<String> ftpPaths;

    public UploadResponseDTO(boolean success, String message, List<String> ftpPaths) {
        this.success = success;
        this.message = message;
        this.ftpPaths = ftpPaths;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getFtpPaths() {
        return ftpPaths;
    }

    public void setFtpPaths(List<String> ftpPaths) {
        this.ftpPaths = ftpPaths;
    }
}