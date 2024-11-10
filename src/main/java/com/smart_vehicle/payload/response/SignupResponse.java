package com.smart_vehicle.payload.response;

public class SignupResponse {

    private String message;
    private ResponseData data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ResponseData getData() {
        return data;
    }

    public void setData(ResponseData data) {
        this.data = data;
    }

    public SignupResponse(String username, String phone) {
        this.data = new ResponseData(username, phone);
        this.message = "User Created Successfully!";
    }

    public SignupResponse(){}
}

class ResponseData {
    private String username;
    private String phone;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public ResponseData(String username, String phone){
        this.username = username;
        this.phone = phone;
    }

    public ResponseData(){}
}
