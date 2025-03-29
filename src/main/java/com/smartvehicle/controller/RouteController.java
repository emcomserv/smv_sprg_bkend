package com.smartvehicle.controller;

import com.smartvehicle.mapper.RoutePointMapper;
import com.smartvehicle.payload.request.*;
import com.smartvehicle.service.RouteService;
import com.smartvehicle.entity.*;
import com.smartvehicle.mapper.RouteMapper;
import com.smartvehicle.payload.response.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/route")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @Autowired
    private RouteMapper routeMapper;
    @Autowired
    private RoutePointMapper routePointMapper;
    @PostMapping("/register")
    public ResponseEntity<RouteRegResDTO> registerRoute(@Valid @RequestBody RouteRegistrationReq request) {
            RouteRegResDTO response = routeService.registerRoute(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("/route-point/register")
    public ResponseEntity<RoutePointResponseDTO> registerRoutePoint(@Valid @RequestBody RoutePointRegistrationReq request) {
        RoutePoint routePoint = routeService.registerRoutePoint(request);

        RoutePointResponseDTO routePointResponseDTO = routePointMapper.toResponseDTO(routePoint);
            return new ResponseEntity<>(routePointResponseDTO, HttpStatus.CREATED);
    }
    /**
     * Get all routes
     */
    @GetMapping
    public ResponseEntity<List<RouteResponseDTO>> getAllRoutes() {
        List<Route> routes = routeService.getAllRoutes();
        List<RouteResponseDTO> routeResponseDTOs = routeMapper.toResponseDTO(routes);
        return ResponseEntity.ok(routeResponseDTOs);
    }

    /**
     * Get a route by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RouteResponseDTO> getRouteById(@PathVariable Long id) {
        Route route = routeService.getRouteById(id);
        RouteResponseDTO routeResponseDTO = routeMapper.toResponseDTO(route);
        return ResponseEntity.ok(routeResponseDTO);
    }

    @GetMapping("/{id}/student-count")
    public ResponseEntity<RouteStudentCountDTO> getRouteStudentCountById(@PathVariable Long id) {
//        Route route = routeService.getRouteById(id);
        Integer routeStudentCount =routeService.getRouteStudentCountById(id);
        return ResponseEntity.ok(new RouteStudentCountDTO(routeStudentCount));
    }

    @GetMapping("/smid/{id}")
    public ResponseEntity<RouteResponseDTO> getRouteById(@PathVariable String id) {
        Route route = routeService.getRouteBySmId(id);
        RouteResponseDTO routeResponseDTO = routeMapper.toResponseDTO(route);
        return ResponseEntity.ok(routeResponseDTO);
    }
    @GetMapping("/admin/{adminId}")
    public ResponseEntity<?> getRoutesByAdminId(@PathVariable Long adminId) {
        List<Route> routes = routeService.getRoutesByAdminId(adminId);
        List<RouteResponseDTO> routeResponseDTOS = routeMapper.toResponseDTO(routes);
        return ResponseEntity.ok(routeResponseDTOS);
    }

    @GetMapping("/sm/admin/{adminSmId}")
    public ResponseEntity<?> getRoutesByAdminUsername(@PathVariable String adminSmId) {
        List<Route> routes = routeService.getRoutesBySMAdminId(adminSmId);
        List<RouteResponseDTO> routeResponseDTOS = routeMapper.toResponseDTO(routes);
        return ResponseEntity.ok(routeResponseDTOS);
    }
    @GetMapping("/school/{schoolId}")
    public ResponseEntity<?> getRoutesBySchoolId(@PathVariable String schoolId) {
        List<Route> routes = routeService.getRoutesBySchoolId(schoolId);
        List<RouteResponseDTO> routeResponseDTOS = routeMapper.toResponseDTO(routes);
        return ResponseEntity.ok(routeResponseDTOS);
    }

    @GetMapping("/passenger-info")
    public ResponseEntity<List<PassengerInfoResponse>> getAllPassengerInfo() {
        List<PassengerInfoResponse> passengerInfoList = routeService.getAllPassengerInfo();
        return ResponseEntity.ok(passengerInfoList);
    }
    @GetMapping("/route-point/{smRoutePointId}/passenger-info")
    public ResponseEntity<List<PassengerInfoResponse>> getAllPassengerInfo(@PathVariable String smRoutePointId) {
        List<PassengerInfoResponse> passengerInfoList = routeService.getAllPassengerInfo(smRoutePointId);
        return ResponseEntity.ok(passengerInfoList);
    }
    @PostMapping("/add/passenger-info")
    public ResponseEntity<PassengerInfoDTO>  addPassengerInfo(@RequestBody PassengerInfoRequest request){
        PassengerInfoDTO passengerInfoDTO= routeService.addPassengerInfo(request);
        return new ResponseEntity<>(passengerInfoDTO, HttpStatus.CREATED);
    }

    //UPDATE API For Route and Route Points
    @PutMapping("update/route-point/{id}")
    public ResponseEntity<?> updateRoutePointInfo(@PathVariable String id, @Valid @RequestBody RoutePointUpdateReq updateReq) {
        return routeService.updateRoutePointInfo(id, updateReq);
    }

    @PutMapping("update/{id}")
    public ResponseEntity<?> updateRouteInfo(@PathVariable String id, @Valid @RequestBody RouteUpdateReq updateReq) {
        return routeService.updateRouteInfo(id, updateReq);
    }

}


