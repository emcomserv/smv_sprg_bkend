package com.smartvehicle.service;


import com.smartvehicle.entity.PassengerInfo;
import com.smartvehicle.entity.Route;
import com.smartvehicle.entity.RoutePoint;
import com.smartvehicle.entity.School;
import com.smartvehicle.exception.ApplicationException;
import com.smartvehicle.mapper.PassengerInfoMapper;
import com.smartvehicle.payload.request.PassengerInfoRequest;
import com.smartvehicle.payload.request.RoutePointRegistrationReq;
import com.smartvehicle.payload.request.RouteRegistrationReq;
import com.smartvehicle.payload.response.PassengerInfoDTO;
import com.smartvehicle.payload.response.PassengerInfoResponse;
import com.smartvehicle.payload.response.RoutePointResponseDTO;
import com.smartvehicle.payload.response.RouteRegResDTO;
import com.smartvehicle.repository.PassengerInfoRepository;
import com.smartvehicle.repository.RoutePointRepository;
import com.smartvehicle.repository.RouteRepository;
import com.smartvehicle.repository.SchoolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RouteService {

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private RoutePointRepository routePointRepository;
    @Autowired
    private PassengerInfoRepository passengerInfoRepository;
    @Autowired
    private PassengerInfoMapper passengerInfoMapper;
    @Autowired
    private SchoolRepository schoolRepository;
    @Transactional
    public RouteRegResDTO registerRoute(RouteRegistrationReq request) {
        if (routeRepository.existsByRouteName(request.getRouteName())) {
            throw new RuntimeException("Error: A route with this name already exists.");
        }
        // Find the school by ID
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Error: School not found with ID " + request.getSchoolId()));
        Route route = new Route();
        route.setRouteName(request.getRouteName());
        route.setTitle(request.getTitle());
        route.setStatus(request.getStatus());
        route.setReserve(request.getReserve());
        route.setSmRouteId(request.getSmRouteId());
        route.setContent(request.getContent());
        route.setSchool(school);
        routeRepository.save(route);
        return new RouteRegResDTO(route.getId(), route.getRouteName(), route.getTitle(), route.getStatus(), school.getName());
    }
    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public Route getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Route not found with ID: " + id));
    }
    public Integer getRouteStudentCountById(Long id) {
        return routeRepository.findRouteStudentCountById(id);
    }
    public RoutePoint getRoutePointById(Long id) {
        return routePointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RoutePoint not found with ID: " + id));
    }
    public Route getRouteBySmId(String id) {
        return routeRepository.findBySmRouteId(id)
                .orElseThrow(() -> new RuntimeException("Route not found with SmRouteId: " + id));
    }

    public List<Route> getRoutesByAdminId(Long adminId) {
        return routeRepository.findRouteByAdminId(adminId);
    }
    public List<Route> getRoutesBySchoolId(String schoolId) {
        return routeRepository.findRouteBySchoolId(schoolId);
    }

    public List<Route> getRoutesBySMAdminId(String smAdminId) {
        return routeRepository.findBySMRoute_Id(smAdminId);
    }


    public List<PassengerInfoResponse> getAllPassengerInfo() {
        return passengerInfoRepository.fetchPassengerInfo();
    }

    @Transactional
    public RoutePoint registerRoutePoint(RoutePointRegistrationReq request) {
        // Validate if the route exists
        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new RuntimeException("Error: Route not found with ID " + request.getRouteId()));

        // Check if a route point with the same sequence order already exists for the route
//        if (routePointRepository.existsByRouteAndSeqOrder(request.getRouteId(), request.getSeqOrder())) {
//            throw new RuntimeException("Error: Route point with sequence order " + request.getSeqOrder() + " already exists for this route.");
//        }

        // Create and save the RoutePoint entity
        RoutePoint routePoint = new RoutePoint();
        routePoint.setSeqOrder(request.getSeqOrder());
        routePoint.setRoute(route);
        routePoint.setRoutePointName(request.getRoutePointName());
        routePoint.setTitle(request.getTitle());
        routePoint.setLatitude(request.getLatitude());
        routePoint.setLongitude(request.getLongitude());
        routePoint.setStatus(request.getStatus());
        routePoint.setReserve(request.getReserve());
        routePoint.setContent(request.getContent());
        routePointRepository.save(routePoint);

        // Return a response DTO
        return routePoint;
    }


    public PassengerInfoDTO addPassengerInfo(PassengerInfoRequest request) {

        boolean isExist = passengerInfoRepository.existsBySmRoutePointIdAndSmStudentId(request.getSmRoutePointId(),request.getSmStudentId());
        if (isExist){
          throw new ApplicationException("Student already boarded for this Route");
        }

        PassengerInfo passengerInfo = new PassengerInfo();
        passengerInfo.setSmRouteId(request.getSmRouteId());
        passengerInfo.setSmRoutePointId(request.getSmRoutePointId());
        passengerInfo.setSmStudentId(request.getSmStudentId());
        passengerInfo.setCreatedAt(LocalDateTime.now());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        passengerInfo.setDate(LocalDateTime.now().format(formatter));
        passengerInfoRepository.save(passengerInfo);
        return passengerInfoMapper.toDTO(passengerInfo);
    }

    public List<PassengerInfoResponse> getAllPassengerInfo(String smRoutePointId) {
        return passengerInfoRepository.fetchPassengerInfo(smRoutePointId);
    }
}
