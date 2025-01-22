package com.smartvehicle.payload.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpResponseDTO {
    private Long id;
    private String phone;
    private String email;
}

