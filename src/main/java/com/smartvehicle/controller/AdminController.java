package com.smartvehicle.controller;

import com.smartvehicle.entity.*;
import com.smartvehicle.mapper.AdminMapper;
import com.smartvehicle.payload.request.AdminSignupReq;
import com.smartvehicle.payload.request.AdminUpdateReq;
import com.smartvehicle.payload.response.AdminResponseDTO;
import com.smartvehicle.payload.response.SignupResponse;
import com.smartvehicle.repository.AdminRepository;
import com.smartvehicle.repository.SchoolRepository;
import com.smartvehicle.repository.UserRepository;
import com.smartvehicle.service.AdminService;
import com.smartvehicle.service.RouteService;
import com.smartvehicle.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private com.smartvehicle.service.SmIdGeneratorService smIdGeneratorService;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody AdminSignupReq request) throws Exception{

        // Check for duplicate smAdminId
        if (adminRepository.findBySmAdminId(request.getSmAdminId()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("sm_admin_id already  exists! Please use a unique value.");
        }

        // Fetch School by ID
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Error: School not found with id " + request.getSchoolId()));

        // Register User
        User user = userService.registerUser(request, UserType.ADMIN.name(), true);

        // Create Admin
        Admin admin = new Admin();
        admin.setUser(user);
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setSchool(school);
        String smAdminId = request.getSmAdminId() != null && !request.getSmAdminId().isEmpty()
                ? request.getSmAdminId()
                : smIdGeneratorService.generateAdminId(request.getSchoolId());
        admin.setSmAdminId(smAdminId);
        adminRepository.save(admin);

        SignupResponse response = new SignupResponse(request.getUsername(), request.getPhone());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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

    @GetMapping
    public ResponseEntity<List<AdminResponseDTO>> getAllAdmins() {
        List<Admin> admins = adminRepository.findAll();
        List<AdminResponseDTO> response = adminMapper.toResponseDTO(admins);
        return ResponseEntity.ok(response);
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
    
    @GetMapping("/by-adminId")
    public ResponseEntity<AdminResponseDTO> getBySmAdminId(@RequestParam String smAdminId) {
        Admin admin = adminRepository.findBySmAdminId(smAdminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with smAdminId: " + smAdminId));
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
    public ResponseEntity<?> updateAdminInfo(@PathVariable String id, @RequestBody AdminUpdateReq updateReq) {
        Optional<Admin> adminOptional = adminRepository.findBySmAdminId(id);
        if (adminOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Admin is not found with Id " + id);
        }

        Admin admin = adminOptional.get();

        // Update Admin entity fields conditionally
        if (updateReq.getFirstName() != null) admin.setFirstName(updateReq.getFirstName());
        if (updateReq.getLastName() != null) admin.setLastName(updateReq.getLastName());

        if (updateReq.getSchoolId() != null && !updateReq.getSchoolId().isEmpty()) {
            Optional<School> school = schoolRepository.findById(updateReq.getSchoolId());
            school.ifPresent(admin::setSchool);
        }

        // Update User entity if needed
        User user = admin.getUser();
        if (updateReq.getPhone() != null) user.setPhone(updateReq.getPhone());
        if (updateReq.getEmail() != null) user.setEmail(updateReq.getEmail());
        userRepository.save(user);

        admin.setUser(user);
        adminRepository.save(admin);

        return ResponseEntity.ok("Admin details updated successfully for ID " + id);
    }

    @PatchMapping("/user/2fa")
    public ResponseEntity<?> updateUser2FA(@RequestParam String username, @RequestParam boolean enabled) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = userOpt.get();
        user.setTwoFactorEnabled(enabled);
        userRepository.save(user);
        return ResponseEntity.ok("2FA updated for user: " + username + ", enabled: " + enabled);
    }
}
