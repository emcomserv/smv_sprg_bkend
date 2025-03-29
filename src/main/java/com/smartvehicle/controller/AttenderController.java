package com.smartvehicle.controller;

import com.smartvehicle.payload.request.AttenderUpdateReq;
import com.smartvehicle.repository.UserRepository;
import com.smartvehicle.service.RouteService;
import com.smartvehicle.service.UserService;
import com.smartvehicle.entity.*;
import com.smartvehicle.mapper.AttenderMapper;
import com.smartvehicle.payload.request.AttenderSignupReq;
import com.smartvehicle.payload.response.AttenderResponseDTO;
import com.smartvehicle.payload.response.SignupResponse;
import com.smartvehicle.repository.AttenderRepository;
import com.smartvehicle.repository.SchoolRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


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
    @Autowired
    UserRepository userRepository;

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


    @GetMapping("/{id}")
    public ResponseEntity<AttenderResponseDTO> getById(@PathVariable Long id) {
        Attender attender = attenderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Attender not found with id  "+id));
        AttenderResponseDTO attenderResponseDTO = attenderMapper.toResponseDTO(attender);
        return ResponseEntity.ok(attenderResponseDTO);
    }

    @PutMapping("/update/{id}")
    @Transactional
    public ResponseEntity<?> updateAttenderInfo(@PathVariable String id, @Valid @RequestBody AttenderUpdateReq updateReq){
        Optional<Attender> attenderOptional = attenderRepository.findBySmAttenderId(id);
        if(!attenderOptional.isPresent())
            return ResponseEntity.ofNullable("Attender is not found with Id " + id);

        //For updating user Details
        Optional<User> userOptional = userRepository.findById(attenderOptional.get().getUser().getId());
        User user = null;
        if(userOptional.isPresent()){
            user = userOptional.get();
            if(updateReq.getPhone() != null && !updateReq.getPhone().isEmpty())
                user.setPhone(updateReq.getPhone());
            if(updateReq.getEmail() != null && !updateReq.getEmail().isEmpty())
                user.setEmail(updateReq.getEmail());

            user = userRepository.save(user);
        }

        Attender attender = new Attender();
        attender.setId(attenderOptional.get().getId());
        attender.setSmAttenderId(id);
        attender.setFirstName(updateReq.getFirstName());
        attender.setLastName(updateReq.getLastName());
        if(updateReq.getRouteId() != null && !updateReq.getRouteId().isEmpty()){
            Route route = routeService.getRouteBySmId(updateReq.getRouteId());
            attender.setRoute(route);
        }
        if(updateReq.getSchoolId() != null && !updateReq.getSchoolId().isEmpty()){
            Optional<School> school = schoolRepository.findById(updateReq.getSchoolId());
            if(school.isPresent())
                attender.setSchool(school.get());
        }
        attender.setUser(user);
        attenderRepository.save(attender);

        return ResponseEntity.ok("Attender updated successfully for ID " + id);
    }

}
