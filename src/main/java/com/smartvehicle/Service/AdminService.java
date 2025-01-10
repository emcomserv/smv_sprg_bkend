package com.smartvehicle.Service;

import com.smartvehicle.entity.Route;
import com.smartvehicle.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {
    @Autowired
    RouteRepository routeRepository;

    public List<Route> findRoutesBySchoolId(String schoolId){
        List<Route> routes = this.routeRepository.findBySchool_Id(schoolId)
                .orElseThrow(() -> new RuntimeException("Error fetching routes"));

        return routes;
    }
}
