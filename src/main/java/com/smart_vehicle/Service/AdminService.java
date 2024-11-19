package com.smart_vehicle.Service;

import com.smart_vehicle.models.Route;
import com.smart_vehicle.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {
    @Autowired
    RouteRepository routeRepository;

    public List<Route> findRoutesBySchoolId(String schoolId){
        List<Route> routes = this.routeRepository.findBySchoolUniqueId(schoolId)
                .orElseThrow(() -> new RuntimeException("Error fetching routes"));

        return routes;
    }
}
