package com.smartvehicle.controller;

import com.smartvehicle.service.RouteService;
import com.smartvehicle.service.UserService;
import com.smartvehicle.entity.*;
import com.smartvehicle.mapper.DriverMapper;
import com.smartvehicle.payload.request.DriverSignupReq;
import com.smartvehicle.payload.response.DriverResponseDTO;
import com.smartvehicle.payload.response.SignupResponse;
import com.smartvehicle.repository.DriverRepository;
import com.smartvehicle.repository.SchoolRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/driver")
@Slf4j
public class DriverController {
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private DriverMapper driverMapper;
    @Autowired
    private RouteService routeService;
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody DriverSignupReq request) throws Exception{
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Error: School not found with id  "+request.getSchoolId()));
            User user = userService.registerUser(request, UserType.DRIVER.name(),false);

            Driver driver = new Driver();
            driver.setUser(user);
            driver.setFirstName(request.getFirstName());
            driver.setLastName(request.getLastName());
            driver.setSchool(school);
            if(request.getRouteId()!=null) {
                Route route = routeService.getRouteById(request.getRouteId());
                driver.setRoute(route);
            }

            driver.setSmDriverId(request.getSmDriverId());
            driverRepository.save(driver);
            SignupResponse response = new SignupResponse(request.getUsername(), request.getPhone());
            return new ResponseEntity<SignupResponse>(response, HttpStatus.CREATED);

    }

    /**
     * Get all routes
     */
    @GetMapping
    public ResponseEntity<List<DriverResponseDTO>> getAllDrivers() {
        List<Driver> drivers = driverRepository.findAll();
        List<DriverResponseDTO> driverResponseDTOS = driverMapper.toResponseDTO(drivers);
        return ResponseEntity.ok(driverResponseDTOS);
    }

    /**
     * Get a route by ID
     */
    @GetMapping("/route/{id}")
    public ResponseEntity<DriverResponseDTO> getByRouteId(@PathVariable Long id) {
        Driver route = driverRepository.findByRoute_Id(id)
                .orElseThrow(() -> new RuntimeException("Error: Driver not found with route id  "+id));
        DriverResponseDTO driverResponseDTO = driverMapper.toResponseDTO(route);
        return ResponseEntity.ok(driverResponseDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponseDTO> getById(@PathVariable Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Driver not found with id  "+id));
        DriverResponseDTO driverResponseDTO = driverMapper.toResponseDTO(driver);
        return ResponseEntity.ok(driverResponseDTO);
    }
}
