package com.smartvehicle.payload.response;

import java.time.LocalDateTime;

public record PassengerInfoDTO(
        String smRouteId,
        String smRoutePointId,
        String smStudentId,
        String date,
        LocalDateTime createdAt
) {}