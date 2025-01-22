package com.smartvehicle.controller;

import com.smartvehicle.Service.RouteService;
import com.smartvehicle.Service.StudentService;
import com.smartvehicle.Service.UserService;
import com.smartvehicle.entity.*;
import com.smartvehicle.mapper.RouteMapper;
import com.smartvehicle.mapper.StudentMapper;
import com.smartvehicle.payload.request.ParentSignupReq;
import com.smartvehicle.payload.request.RoutePointRegistrationReq;
import com.smartvehicle.payload.request.RouteRegistrationReq;
import com.smartvehicle.payload.request.SchoolRegistrationReq;
import com.smartvehicle.payload.response.*;
import com.smartvehicle.repository.ParentRepository;
import com.smartvehicle.repository.SchoolRepository;
import com.smartvehicle.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/route")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @Autowired
    private RouteMapper routeMapper;

    @PostMapping("/register")
    public ResponseEntity<RouteRegResDTO> registerRoute(@Valid @RequestBody RouteRegistrationReq request) {
            RouteRegResDTO response = routeService.registerRoute(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("/route-point/register")
    public ResponseEntity<RoutePointResponseDTO> registerRoutePoint(@Valid @RequestBody RoutePointRegistrationReq request) {
        RoutePointResponseDTO response = routeService.registerRoutePoint(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
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
}


