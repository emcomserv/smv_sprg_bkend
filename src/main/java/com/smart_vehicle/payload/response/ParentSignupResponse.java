package com.smart_vehicle.payload.response;

public class ParentSignupResponse {

    private String email;
    private String contactNum;

    private boolean emailOTP;

    public ParentSignupResponse(String email, String phone) {
        super();
        this.email = email;
        this.contactNum = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getcontactNum() {
        return contactNum;
    }

    public void setcontactNum(String contactNum) {
        this.contactNum = contactNum;
    }

    public boolean isEmailOTP() {
        return emailOTP;
    }

    public void setEmailOTP(boolean emailOTP) {
        this.emailOTP = emailOTP;
    }

    @Override
    public String toString() {
        return "SignupResponse [email=" + email + ", contactNum=" + contactNum + ", emailOTP=" + emailOTP + "]";
    }
}
