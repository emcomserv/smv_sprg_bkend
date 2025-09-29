package com.smartvehicle.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactInfoCreateReq {

    @NotBlank
    @Size(max = 20)
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^[0-9]+$", message = "Contact number must contain only digits")
    @Size(min = 5, max = 20)
    private String contactNumber;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(max = 20)
    private String schoolName;

    @NotBlank
    @Size(max = 200)
    private String message;
}


