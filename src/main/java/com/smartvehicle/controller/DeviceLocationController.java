package com.smartvehicle.controller;

import com.smartvehicle.payload.response.DeviceLocationResponse;
import com.smartvehicle.service.DeviceLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/device-locations")
public class DeviceLocationController {

    @Autowired
    private DeviceLocationService deviceLocationService;

    @GetMapping
    public ResponseEntity<List<DeviceLocationResponse>> getBySchoolRouteAndDate(
            @RequestParam String schoolId,
            @RequestParam String routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "period", required = false) String period
    ) {
        return ResponseEntity.ok(deviceLocationService.getBySchoolRouteAndDate(schoolId, routeId, date, period));
    }
}


