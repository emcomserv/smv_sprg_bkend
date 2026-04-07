package com.smartvehicle.controller;

import com.smartvehicle.entity.Route;
import com.smartvehicle.entity.Driver;
import com.smartvehicle.entity.RoutePoint;
import com.smartvehicle.mapper.RouteMapper;
import com.smartvehicle.mapper.RoutePointMapper;
import com.smartvehicle.payload.request.*;
import com.smartvehicle.payload.response.*;
import com.smartvehicle.repository.RoutePointRepository;
import com.smartvehicle.service.RouteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.smartvehicle.repository.SchoolRepository;
import com.smartvehicle.repository.RouteRepository;

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
    @Autowired
    private RoutePointRepository routePointRepository;

    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private RouteRepository routeRepository;
    @Autowired
    private com.smartvehicle.service.SmIdGeneratorService smIdGeneratorService;

    @PostMapping("/register")
    public ResponseEntity<RouteRegResDTO> registerRoute(@Valid @RequestBody RouteRegistrationReq request) {
            // Auto-generate smRouteId if missing
            if (request.getSmRouteId() == null || request.getSmRouteId().isEmpty()) {
                String gen = smIdGeneratorService.generateRouteId(request.getSchoolId());
                request.setSmRouteId(gen);
            }
            RouteRegResDTO response = routeService.registerRoute(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("/route-point/register")
    public ResponseEntity<RoutePointResponseDTO> registerRoutePoint(@Valid @RequestBody RoutePointRegistrationReq request) {
        if (request.getSmRoutePointId() == null || request.getSmRoutePointId().isEmpty()) {
            String gen = smIdGeneratorService.generateRoutePointId(request.getSchoolId());
            request.setSmRoutePointId(gen);
        }
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

    @GetMapping("/mine")
    public ResponseEntity<List<RouteResponseDTO>> getMyRoutes(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof java.util.Map)) {
            throw new RuntimeException("Unauthorised User");
        }
        java.util.Map<String, Object> details = (java.util.Map<String, Object>) authentication.getDetails();
        Long userId = (Long) details.get("userId");
        List<com.smartvehicle.entity.School> schools = schoolRepository.findByCreatedBy(userId);
        List<String> schoolIds = schools.stream().map(com.smartvehicle.entity.School::getId).toList();
        List<Route> routes = schoolIds.isEmpty() ? java.util.List.of() : routeRepository.findBySchool_IdIn(schoolIds);
        return ResponseEntity.ok(routeMapper.toResponseDTO(routes));
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

    // Get routes by driver smDriverId
    @GetMapping("/driver/smid/{smDriverId}")
    public ResponseEntity<List<RouteResponseDTO>> getRoutesByDriverSmId(@PathVariable String smDriverId) {
        List<Route> routes = routeService.getRoutesByDriverSmId(smDriverId);
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

    @GetMapping("/{routeId}/route-points")
    public ResponseEntity<List<RoutePointResponseDTO>> getRoutePointsByRouteId(@PathVariable Long routeId) {
        List<RoutePoint> routePoints = routePointRepository.findByRoute_Id(routeId);
        List<RoutePointResponseDTO> response = routePointMapper.toResponseDTO(routePoints);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/smid/{smRouteId}/route-points")
    public ResponseEntity<List<RoutePointResponseDTO>> getRoutePointsBySmRouteId(@PathVariable String smRouteId) {
        List<RoutePoint> routePoints = routePointRepository.findByRoute_SmRouteId(smRouteId);
        List<RoutePointResponseDTO> response = routePointMapper.toResponseDTO(routePoints);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/route-points")
    public ResponseEntity<List<RoutePointResponseDTO>> getAllRoutePoints() {
        List<RoutePoint> routePoints = routePointRepository.findAll();
        List<RoutePointResponseDTO> response = routePointMapper.toResponseDTO(routePoints);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/route-point/by-id")
    public ResponseEntity<RoutePointResponseDTO> getRoutePointBySmRoutePointId(@RequestParam String smRoutePointId) {
        RoutePoint routePoint = routePointRepository.findBySmRoutePointId(smRoutePointId)
                .orElseThrow(() -> new RuntimeException("RoutePoint not found with smRoutePointId: " + smRoutePointId));
        RoutePointResponseDTO response = routePointMapper.toResponseDTO(routePoint);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-routeId")
    public ResponseEntity<RouteResponseDTO> getRouteBySmRouteId(@RequestParam String smRouteId) {
        Route route = routeService.getRouteBySmId(smRouteId);
        RouteResponseDTO routeResponseDTO = routeMapper.toResponseDTO(route);
        return ResponseEntity.ok(routeResponseDTO);
    }

}


