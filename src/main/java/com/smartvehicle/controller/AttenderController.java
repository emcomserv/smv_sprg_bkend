package com.smartvehicle.controller;

import com.smartvehicle.Service.RouteService;
import com.smartvehicle.Service.UserService;
import com.smartvehicle.entity.*;
import com.smartvehicle.mapper.AttenderMapper;
import com.smartvehicle.payload.request.AttenderSignupReq;
import com.smartvehicle.payload.response.AttenderResponseDTO;
import com.smartvehicle.payload.response.ErrorResponse;
import com.smartvehicle.payload.response.SignupResponse;
import com.smartvehicle.repository.AttenderRepository;
import com.smartvehicle.repository.SchoolRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/attender")
public class AttenderController {
    @Autowired
    private AttenderRepository attenderRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private RouteService routeService;
    @Autowired
    private AttenderMapper attenderMapper;
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AttenderSignupReq request) throws Exception{
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Error: School not found with id  "+request.getSchoolId()));
            User user = userService.registerUser(request, UserType.ATTENDER.name(),false);
            Route route = routeService.getRouteById(request.getRouteId());
            Attender attender = new Attender();
            attender.setUser(user);
            attender.setFirstName(request.getFirstName());
            attender.setLastName(request.getLastName());
            attender.setSchool(school);
            attenderRepository.save(attender);
            SignupResponse response = new SignupResponse(request.getUsername(), request.getPhone());
            return new ResponseEntity<SignupResponse>(response, HttpStatus.CREATED);
    }

    /**
     * Get all routes
     */
    @GetMapping
    public ResponseEntity<List<AttenderResponseDTO>> getAllAttenders() {
        List<Attender> attenders = attenderRepository.findAll();
        List<AttenderResponseDTO> attenderResponseDTOS = attenderMapper.toResponseDTO(attenders);
        return ResponseEntity.ok(attenderResponseDTOS);
    }

    /**
     * Get a route by ID
     */
    @GetMapping("/route/{id}")
    public ResponseEntity<AttenderResponseDTO> getByRouteId(@PathVariable Long id) {
        Attender route = attenderRepository.findByRoute_Id(id)
                .orElseThrow(() -> new RuntimeException("Error: Attender not found with route id  "+id));
        AttenderResponseDTO attenderResponseDTO = attenderMapper.toResponseDTO(route);
        return ResponseEntity.ok(attenderResponseDTO);
    }


}
