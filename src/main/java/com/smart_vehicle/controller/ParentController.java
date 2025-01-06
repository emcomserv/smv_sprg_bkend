package com.smart_vehicle.controller;

import com.smart_vehicle.Service.StudentService;
import com.smart_vehicle.models.Parent;
import com.smart_vehicle.models.Student;
import com.smart_vehicle.payload.response.AuthenticationDetailsResponse;
import com.smart_vehicle.repository.ParentRepository;
import com.smart_vehicle.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/parent")
public class ParentController {
    @Autowired
    private StudentService studentService;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private JwtUtils jwtUtils;
    @GetMapping("/students")
    public ResponseEntity<?> getStudents(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof Map)) {
            throw new RuntimeException("Unauthorised User");
        }

        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        String userId = (String) details.get("userId");

        Parent parent = parentRepository.findByUserId(userId);
        if (parent == null) {
            throw new RuntimeException("Parent not found");
        }

        List<Student> students = studentService.findStudentsByParentId(parent.getParentId());
        return ResponseEntity.ok(students);
    }


}
