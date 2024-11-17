package com.smart_vehicle.payload.response;

import org.springframework.http.HttpStatus;

public class ErrorResponse {
    private String message;

    private HttpStatus status;

    private Object data = null;

    public  ErrorResponse(String message, HttpStatus status){
        this.message = message;
        this.status = status;
    }

    public  ErrorResponse(String message, HttpStatus status,Object data){
        this.message = message;
        this.status = status;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
