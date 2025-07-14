package com.smartvehicle.controller;

import com.smartvehicle.entity.*;
import com.smartvehicle.mapper.DriverMapper;
import com.smartvehicle.payload.request.DriverSignupReq;
import com.smartvehicle.payload.request.DriverUpdateReq;
import com.smartvehicle.payload.response.DriverResponseDTO;
import com.smartvehicle.payload.response.SignupResponse;
import com.smartvehicle.repository.DriverRepository;
import com.smartvehicle.repository.SchoolRepository;
import com.smartvehicle.repository.UserRepository;
import com.smartvehicle.service.RouteService;
import com.smartvehicle.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


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
    @Autowired
    private UserRepository userRepository;
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody DriverSignupReq request) throws Exception{
        // Check for duplicate smDriverId
        if (driverRepository.findBySmDriverId(request.getSmDriverId()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("smDriverId already exists! Please use a unique value.");
        }
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Error: School not found with id  "+request.getSchoolId()));
        User user = userService.registerUser(request, UserType.DRIVER.name(),false);

        Driver driver = new Driver();
        driver.setUser(user);
        driver.setFirstName(request.getFirstName());
        driver.setLastName(request.getLastName());
        driver.setSchool(school);
        driver.setSmDriverId(request.getSmDriverId());
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

    @PutMapping("/update/{id}")
    @Transactional
    public ResponseEntity<?> updateDriverInfo(@PathVariable String id, @Valid @RequestBody DriverUpdateReq updateReq) {
        Optional<Driver> driverOptional = driverRepository.findBySmDriverId(id);
        if (!driverOptional.isPresent()) {
            return ResponseEntity.ofNullable("Driver is not found with Id " + id);
        }

        Driver driver = driverOptional.get();

        // Update user details
        Optional<User> userOptional = userRepository.findById(driver.getUser().getId());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (updateReq.getPhone() != null && !updateReq.getPhone().isEmpty())
                user.setPhone(updateReq.getPhone());
            if (updateReq.getEmail() != null && !updateReq.getEmail().isEmpty())
                user.setEmail(updateReq.getEmail());

            user = userRepository.save(user);
            driver.setUser(user);
        }

        if (updateReq.getFirstName() != null) {
            driver.setFirstName(updateReq.getFirstName());
        }
        if (updateReq.getLastName() != null) {
            driver.setLastName(updateReq.getLastName());
        }

        if (updateReq.getSchoolId() != null && !updateReq.getSchoolId().isEmpty()) {
            Optional<School> school = schoolRepository.findById(updateReq.getSchoolId());
            school.ifPresent(driver::setSchool);
        }

        if (updateReq.getRouteId() != null && !updateReq.getRouteId().isEmpty()) {
            Route route = routeService.getRouteBySmId(updateReq.getRouteId());
            driver.setRoute(route);
        }

        driverRepository.save(driver);
        return ResponseEntity.ok("Driver details updated successfully for ID " + id);
    }

}
