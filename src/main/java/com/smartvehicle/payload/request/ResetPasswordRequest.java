package com.smartvehicle.payload.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {


    @NotBlank
    @Size(min = 6, max = 100,message = "Password should have minimum of 6 characters")
    private String password;


}
