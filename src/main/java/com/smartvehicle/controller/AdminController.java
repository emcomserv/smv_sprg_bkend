package com.smartvehicle.controller;

import com.smartvehicle.Service.AdminService;
import com.smartvehicle.Service.UserService;
import com.smartvehicle.entity.*;
import com.smartvehicle.payload.request.AdminSignupReq;
import com.smartvehicle.payload.request.ParentSignupReq;
import com.smartvehicle.payload.response.ErrorResponse;
import com.smartvehicle.payload.response.SignupResponse;
import com.smartvehicle.repository.AdminRepository;
import com.smartvehicle.repository.SchoolRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    @Autowired
    AdminRepository adminRepository;

    @Autowired
    AdminService adminService;
    @Autowired
    private UserService userService;
    @Autowired
    private SchoolRepository schoolRepository;
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody AdminSignupReq request) throws Exception{
        try {
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Error: School not found with id  "+request.getSchoolId()));
            User user = userService.registerUser(request, ERole.ADMIN.name(),true);
            Admin admin = new Admin();
            admin.setUser(user);
            admin.setFirstName(request.getFirstName());
            admin.setLastName(request.getLastName());
            admin.setSchool(school);
            adminRepository.save(admin);
            SignupResponse response = new SignupResponse(request.getUsername(), request.getPhone());
            return new ResponseEntity<SignupResponse>(response, HttpStatus.CREATED);
        }catch(Exception error){
            return new ResponseEntity<ErrorResponse>(new ErrorResponse(error.getMessage(),
                    HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("routes")
    public ResponseEntity<?> getRoutes(Authentication authentication){
//        if (authentication == null || !(authentication.getDetails() instanceof Map)) {
//            throw new RuntimeException("Unauthorised User");
//        }

        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Long userId = (Long) details.get("userId");

        Admin admin = this.adminRepository.findByUser_Id(userId)
                .orElseThrow(() ->  new RuntimeException("Admin not found"));

        List<Route> routes = this.adminService.findRoutesBySchoolId(admin.getSchool().getId());

        return ResponseEntity.ok(routes);

    }
}
