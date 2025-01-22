package com.smartvehicle.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlertReq {
    private String alertType;
    private String title;
    private String body;
    private Long userId;
}
