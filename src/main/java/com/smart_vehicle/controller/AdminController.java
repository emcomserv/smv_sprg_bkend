package com.smart_vehicle.controller;

import com.smart_vehicle.Service.AdminService;
import com.smart_vehicle.models.Admin;
import com.smart_vehicle.models.Route;
import com.smart_vehicle.models.User;
import com.smart_vehicle.payload.request.VerifyOTPRequest;
import com.smart_vehicle.payload.response.ErrorResponse;
import com.smart_vehicle.payload.response.JwtResponse;
import com.smart_vehicle.repository.AdminRepository;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    @Autowired
    AdminRepository adminRepository;

    @Autowired
    AdminService adminService;



    @GetMapping("routes")
    public ResponseEntity<?> getRoutes(Authentication authentication){
        if (authentication == null || !(authentication.getDetails() instanceof Map)) {
            throw new RuntimeException("Unauthorised User");
        }

        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        String userId = (String) details.get("userId");

        Admin admin = this.adminRepository.findByUserId(userId)
                .orElseThrow(() ->  new RuntimeException("Admin not found"));

        List<Route> routes = this.adminService.findRoutesBySchoolId(admin.getSchoolUniqueId());

        return ResponseEntity.ok(routes);

    }
}
