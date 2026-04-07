package com.smartvehicle.service;

import com.smartvehicle.entity.DeviceLocation;
import com.smartvehicle.payload.response.DeviceLocationResponse;
import com.smartvehicle.repository.DeviceLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DeviceLocationService {

    @Autowired
    private DeviceLocationRepository deviceLocationRepository;

    public List<DeviceLocationResponse> getBySchoolRouteAndDate(String schoolId, String routeId, LocalDate date, String period) {
        LocalDateTime start;
        LocalDateTime end;
        if (period != null && period.equalsIgnoreCase("morning")) {
            start = date.atStartOfDay();
            end = date.atTime(11, 59, 59, 999_999_999);
        } else if (period != null && period.equalsIgnoreCase("evening")) {
            start = date.atTime(12, 0);
            end = LocalDateTime.of(date, LocalTime.MAX);
        } else {
            start = date.atStartOfDay();
            end = LocalDateTime.of(date, LocalTime.MAX);
        }
        List<DeviceLocation> list = deviceLocationRepository
                .findBySchoolIdAndRouteIdAndEventTimeBetweenOrderByEventTimeAsc(schoolId, routeId, start, end);
        List<DeviceLocationResponse> out = new ArrayList<>();
        for (DeviceLocation dl : list) {
            out.add(new DeviceLocationResponse(
                    dl.getId(),
                    dl.getDeviceId(),
                    dl.getSchoolId(),
                    dl.getRouteId(),
                    dl.getLatitude(),
                    dl.getLongitude(),
                    dl.getEventTime()
            ));
        }
        return out;
    }
}


