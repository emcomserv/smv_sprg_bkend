package com.smartvehicle.controller;

import com.smartvehicle.payload.request.AdminUpdateReq;
import com.smartvehicle.payload.request.AttenderUpdateReq;
import com.smartvehicle.repository.UserRepository;
import com.smartvehicle.service.AdminService;
import com.smartvehicle.service.RouteService;
import com.smartvehicle.service.UserService;
import com.smartvehicle.entity.*;
import com.smartvehicle.mapper.AdminMapper;
import com.smartvehicle.payload.request.AdminSignupReq;
import com.smartvehicle.payload.response.AdminResponseDTO;
import com.smartvehicle.payload.response.SignupResponse;
import com.smartvehicle.repository.AdminRepository;
import com.smartvehicle.repository.SchoolRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    @Autowired
    private RouteService routeService;
    @Autowired
    private AdminMapper adminMapper ;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody AdminSignupReq request) throws Exception{

            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Error: School not found with id  "+request.getSchoolId()));
            User user = userService.registerUser(request, UserType.ADMIN.name(),true);
            Admin admin = new Admin();
            admin.setUser(user);
            admin.setFirstName(request.getFirstName());
            admin.setLastName(request.getLastName());
            admin.setSchool(school);
            if(request.getRouteId()!=null){
                Route route = routeService.getRouteById(request.getRouteId());
                List<Route> routes = new ArrayList<>();
                routes.add(route);
                admin.setRoutes(routes);
            }
            adminRepository.save(admin);
            SignupResponse response = new SignupResponse(request.getUsername(), request.getPhone());
            return new ResponseEntity<SignupResponse>(response, HttpStatus.CREATED);

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

    @GetMapping("/route/{routeId}")
    public ResponseEntity<?> getAdminByRouteId(@PathVariable Long routeId) {
        List<Admin> admins  = adminRepository.findByRoute_Id(routeId);
        List<AdminResponseDTO> adminResponseDTOS = adminMapper.toResponseDTO(admins);
        return ResponseEntity.ok(adminResponseDTOS);
    }

    @GetMapping("/school/{schoolId}")
    public ResponseEntity<?> getAdminBySchoolId(@PathVariable String schoolId) {
        List<Admin> admins  = adminRepository.findBySchool_Id(schoolId);
        List<AdminResponseDTO> adminResponseDTOS = adminMapper.toResponseDTO(admins);
        return ResponseEntity.ok(adminResponseDTOS);
    }

    @GetMapping("/{adminId}")
    public ResponseEntity<?> getById(@PathVariable Long adminId) {
        Admin admin  = adminRepository.findById(adminId)
                .orElseThrow(() ->  new RuntimeException("Admin not found with ID "+adminId));
        AdminResponseDTO adminResponseDTO = adminMapper.toResponseDTO(admin);
        return ResponseEntity.ok(adminResponseDTO);
    }
    @GetMapping("/sm/route/{routeSmId}")
    public ResponseEntity<?> getByRouteId(@PathVariable String routeSmId) {
        List<Admin> admins  = adminRepository.findBySMRoute_Id(routeSmId);
        List<AdminResponseDTO> adminResponseDTOS = adminMapper.toResponseDTO(admins);
        return ResponseEntity.ok(adminResponseDTOS);
    }

    @PutMapping("/update/{id}")
    @Transactional
    public ResponseEntity<?> updateAdminInfo(@PathVariable String id, @Valid @RequestBody AdminUpdateReq updateReq){
        Optional<Admin> adminOptional = adminRepository.findBySmAdminId(id);
        if(!adminOptional.isPresent())
            return ResponseEntity.ofNullable("Admin is not found with Id " + id);

        //For updating user Details
        Optional<User> userOptional = userRepository.findById(adminOptional.get().getUser().getId());
        User user = null;
        if(userOptional.isPresent()){
            user = userOptional.get();
            if(updateReq.getPhone() != null && !updateReq.getPhone().isEmpty())
                user.setPhone(updateReq.getPhone());
            if(updateReq.getEmail() != null && !updateReq.getEmail().isEmpty())
                user.setEmail(updateReq.getEmail());

            user = userRepository.save(user);
        }

        Admin admin = new Admin();
        admin.setId(adminOptional.get().getId());
        admin.setSmAdminId(id);
        admin.setFirstName(updateReq.getFirstName());
        admin.setLastName(updateReq.getLastName());

        if(updateReq.getSchoolId() != null && !updateReq.getSchoolId().isEmpty()){
            Optional<School> school = schoolRepository.findById(updateReq.getSchoolId());
            if(school.isPresent())
                admin.setSchool(school.get());
        }
        admin.setUser(user);
        adminRepository.save(admin);

        return ResponseEntity.ok("Admin details updated successfully for ID " + id);
    }
}
