package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private Boolean twoFactorEnabled;
    private Boolean status;
}

