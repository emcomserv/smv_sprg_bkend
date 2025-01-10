package com.smartvehicle.payload.request;


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

    @NotBlank
    @Size(min = 10, max = 15,message = "Phone number should be minimum of 10 digits and shouldn't exceed 15")
    private String phone;

    @NotBlank
    @Email(message = "Email should be valid")
    private String email;

    private String firstName;
    private String lastName;
    private String countryCode;
    private String schoolId;
}
