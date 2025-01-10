package com.smartvehicle.controller;

import com.smartvehicle.Service.UserService;
import com.smartvehicle.entity.*;
import com.smartvehicle.payload.request.DriverSignupReq;
import com.smartvehicle.payload.response.ErrorResponse;
import com.smartvehicle.payload.response.SignupResponse;
import com.smartvehicle.repository.AttenderRepository;
import com.smartvehicle.repository.DriverRepository;
import com.smartvehicle.repository.SchoolRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/attender")
public class AttenderController {
    @Autowired
    private AttenderRepository attenderRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private SchoolRepository schoolRepository;
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody DriverSignupReq request) throws Exception{
        try {
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Error: School not found with id  "+request.getSchoolId()));
            User user = userService.registerUser(request,ERole.ATTENDER.name(),false);
            Attender attender = new Attender();
            attender.setUser(user);
            attender.setFirstName(request.getFirstName());
            attender.setLastName(request.getLastName());
            attender.setSchool(school);
            attenderRepository.save(attender);
            SignupResponse response = new SignupResponse(request.getUsername(), request.getPhone());
            return new ResponseEntity<SignupResponse>(response, HttpStatus.CREATED);
        }catch(Exception error){
            return new ResponseEntity<ErrorResponse>(new ErrorResponse(error.getMessage(),
                    HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
        }
    }




}
