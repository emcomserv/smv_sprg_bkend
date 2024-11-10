package com.smart_vehicle.payload.request;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ParentSignupRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    private String parentName; // Parent name, size 20

    @NotBlank
    @Size(min = 3, max = 100)
    private String userName; // Username, size 10

    @NotBlank
    @Size(min = 10, max = 100)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    @Size(min = 10, max = 15)
    private String contactNum;

    // Getters and Setters


    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getContactNum() {
        return contactNum;
    }

    public void setContactNum(String contactNum) {
        this.contactNum = contactNum;
    }

    @Override
    public String toString() {
        return "ParentSignupRequest [ parentName=" + parentName
                + ", userName=" + userName + ", contactNum=" +contactNum +  ", email=" +email +  ", password=" +password +  "]";
    }

}
