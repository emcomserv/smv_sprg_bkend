package com.smartvehicle.controller;

import com.smartvehicle.mapper.ParentMapper;
import com.smartvehicle.payload.response.ParentResponseDTO;
import com.smartvehicle.payload.response.ParentResponseUrDTO;
import com.smartvehicle.service.StudentService;
import com.smartvehicle.service.UserService;
import com.smartvehicle.entity.*;
import com.smartvehicle.mapper.StudentMapper;
import com.smartvehicle.payload.request.ParentSignupReq;
import com.smartvehicle.payload.response.SignupResponse;
import com.smartvehicle.repository.ParentRepository;
import com.smartvehicle.repository.SchoolRepository;
import com.smartvehicle.security.jwt.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @Autowired
    private UserService userService;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private ParentMapper parentMapper;
    @Autowired
    private com.smartvehicle.service.SmIdGeneratorService smIdGeneratorService;
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody ParentSignupReq request) throws Exception{
        // Check for duplicate smParentId
        if (parentRepository.findBySmParentId(request.getSmParentId()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("smParentId already exists! Please use a unique value.");
        }
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new RuntimeException("Error: School not found with id  "+request.getSchoolId()));
        User user = userService.registerUser(request, UserType.PARENT.name(),false);
        Parent parent = new Parent();
        parent.setUser(user);
        parent.setFirstName(request.getFirstName());
        parent.setLastName(request.getLastName());
        parent.setSchool(school);
        parent.setCountryCode(request.getCountryCode());
        String smParentId = request.getSmParentId() != null && !request.getSmParentId().isEmpty()
                ? request.getSmParentId()
                : smIdGeneratorService.generateParentId(request.getSchoolId());
        parent.setSmParentId(smParentId);
        parentRepository.save(parent);
        SignupResponse response = new SignupResponse(request.getUsername(), request.getPhone());
        return new ResponseEntity<SignupResponse>(response, HttpStatus.CREATED);
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
        return ResponseEntity.ok(studentMapper.toResponseDTO(students));
    }

    @PutMapping("/update")
    public ResponseEntity<ParentResponseUrDTO> updateParentBySmParentId(
            @RequestParam String smParentId,
            @RequestBody ParentSignupReq request) {

        // Find the parent by sm_parent_id
        Parent parent = parentRepository.findBySmParentId(smParentId)
                .orElseThrow(() -> new RuntimeException("Parent not found with sm_parent_id: " + smParentId));

        // Update fields if present in request
        if (request.getFirstName() != null) {
            parent.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            parent.setLastName(request.getLastName());
        }

        if (request.getCountryCode() != null) {
            parent.setCountryCode(request.getCountryCode());
        }

        if (request.getSchoolId() != null) {
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new RuntimeException("School not found with ID: " + request.getSchoolId()));
            parent.setSchool(school);
        }

        // Save the updated parent entity
        Parent updatedParent = parentRepository.save(parent);

        // Map to ParentResponseDTO using MapStruct
        ParentResponseUrDTO responseDTO = parentMapper.toResponseUrDTO(updatedParent);

        // Return the DTO as response
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/by-parentId")
    public ResponseEntity<ParentResponseDTO> getParentBySmParentId(@RequestParam String smParentId) {
        Parent parent = parentRepository.findBySmParentId(smParentId)
                .orElseThrow(() -> new RuntimeException("Parent not found with sm_parent_id: " + smParentId));
        ParentResponseDTO responseDTO = parentMapper.toResponseDTO(parent);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/by-school")
    public ResponseEntity<List<ParentResponseDTO>> getParentsBySchoolId(@RequestParam String schoolId) {
        List<Parent> parents = parentRepository.findBySchool_Id(schoolId);
        List<ParentResponseDTO> response = parentMapper.toResponseDTO(parents);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ParentResponseDTO>> getAllParents() {
        List<Parent> parents = parentRepository.findAll();
        List<ParentResponseDTO> response = parentMapper.toResponseDTO(parents);
        return ResponseEntity.ok(response);
    }

}
