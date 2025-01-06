package com.smart_vehicle.payload.request;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank
    @Size(min = 3, max = 100, message = "Username should contain minimum 10 characters")
    private String username;

    @NotBlank
    @Size(min = 6, max = 100,message = "Password should have minimum of 6 characters")
    private String password;

    private Set<String> role;

    @NotBlank
    @Size(min = 10, max = 15,message = "Phone number should be minimum of 10 digits and shouldn't exceed 15")
    private String phone;



}
