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

    public List<DeviceLocationResponse> getBySchoolRouteAndDate(String schoolId, String routeId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
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


