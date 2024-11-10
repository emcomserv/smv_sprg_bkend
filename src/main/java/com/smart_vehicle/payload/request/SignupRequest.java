package com.smart_vehicle.payload.request;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupRequest {

    @NotBlank
    @Size(min = 10, max = 100, message = "Username should contain minimum 10 characters")
    private String username;

    @NotBlank
    @Size(min = 6, max = 100,message = "Password should have minimum of 6 characters")
    private String password;

    @NotBlank
    @Size(min = 10, max = 15,message = "Phone number should be minimum of 10 digits and shouldn't exceed 15")
    private String phone;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "SignupRequest [username=" + username
                +", password=" + password + ", phone=" + phone + "]";
    }

}
