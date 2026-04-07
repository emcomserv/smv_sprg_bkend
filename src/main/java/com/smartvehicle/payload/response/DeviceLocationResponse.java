package com.smartvehicle.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceLocationResponse {
    private Long id;
    private String deviceId;
    private String schoolId;
    private String routeId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime eventTime;
}


