package com.smartvehicle.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseLtDTO {
    private Long id;
    private String username;
    private String phone;
    private String email;
    private String status;
}

