package com.smart_vehicle.payload.request;

import jakarta.validation.constraints.NotBlank;

public class VerifyOTPRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String otp;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
