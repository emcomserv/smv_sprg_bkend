package com.smartvehicle.controller;

import com.smartvehicle.Service.StudentService;
import com.smartvehicle.Service.UserService;
import com.smartvehicle.entity.*;
import com.smartvehicle.payload.request.ParentSignupReq;
import com.smartvehicle.payload.request.SignupRequest;
import com.smartvehicle.payload.response.ErrorResponse;
import com.smartvehicle.payload.response.MessageResponse;
import com.smartvehicle.payload.response.SignupResponse;
import com.smartvehicle.repository.ParentRepository;
import com.smartvehicle.repository.RoleRepository;
import com.smartvehicle.repository.SchoolRepository;
import com.smartvehicle.repository.UserRepository;
import com.smartvehicle.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@RestController
@RequestMapping("/api/v1/parent")
public class ParentController {


    @Autowired
    private StudentService studentService;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserService userService;
    @Autowired
    private SchoolRepository schoolRepository;
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody ParentSignupReq request) throws Exception{
        try {
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("Error: School not found with id  "+request.getSchoolId()));
            User user = userService.registerUser(request,ERole.PARENT.name(),false);
            Parent parent = new Parent();
            parent.setUser(user);
            parent.setFirstName(request.getFirstName());
            parent.setLastName(request.getLastName());
            parent.setSchool(school);
            parent.setCountryCode(request.getCountryCode());
            parentRepository.save(parent);
            SignupResponse response = new SignupResponse(request.getUsername(), request.getPhone());
            return new ResponseEntity<SignupResponse>(response, HttpStatus.CREATED);
        }catch(Exception error){
            return new ResponseEntity<ErrorResponse>(new ErrorResponse(error.getMessage(),
                    HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/student")
    @PreAuthorize("hasAnyAuthority('PARENT')")
    public ResponseEntity<?> getStudents(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof Map)) {
            throw new RuntimeException("Unauthorised User");
        }

        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Long userId = (Long) details.get("userId");

        Parent parent = parentRepository.findByUser_Id(userId);
        if (parent == null) {
            throw new RuntimeException("Parent not found");
        }

        List<Student> students = studentService.findStudentsByParentId(parent.getId());
        return ResponseEntity.ok(students);
    }


}
