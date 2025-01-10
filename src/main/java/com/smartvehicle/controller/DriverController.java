package com.smartvehicle.controller;

import com.smartvehicle.Service.UserService;
import com.smartvehicle.entity.*;
import com.smartvehicle.payload.request.DriverSignupReq;
import com.smartvehicle.payload.request.ParentSignupReq;
import com.smartvehicle.payload.response.ErrorResponse;
import com.smartvehicle.payload.response.SignupResponse;
import com.smartvehicle.repository.DriverRepository;
import com.smartvehicle.repository.SchoolRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/driver")
public class DriverController {
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private SchoolRepository schoolRepository;
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody DriverSignupReq request) throws Exception{
        try {
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Error: School not found with id  "+request.getSchoolId()));
            User user = userService.registerUser(request,ERole.DRIVER.name(),false);
            Driver driver = new Driver();
            driver.setUser(user);
            driver.setFirstName(request.getFirstName());
            driver.setLastName(request.getLastName());
            driver.setSchool(school);
            driverRepository.save(driver);
            SignupResponse response = new SignupResponse(request.getUsername(), request.getPhone());
            return new ResponseEntity<SignupResponse>(response, HttpStatus.CREATED);
        }catch(Exception error){
            return new ResponseEntity<ErrorResponse>(new ErrorResponse(error.getMessage(),
                    HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
        }
    }




}
