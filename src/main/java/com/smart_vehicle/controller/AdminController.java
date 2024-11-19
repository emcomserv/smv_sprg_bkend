package com.smart_vehicle.controller;

import com.smart_vehicle.Service.AdminService;
import com.smart_vehicle.models.Admin;
import com.smart_vehicle.models.Route;
import com.smart_vehicle.repository.AdminRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
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
