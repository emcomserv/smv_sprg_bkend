package com.smartvehicle.controller;

import com.smartvehicle.service.StudentService;
import com.smartvehicle.service.UserService;
import com.smartvehicle.entity.*;
import com.smartvehicle.mapper.SchoolMapper;
import com.smartvehicle.payload.request.SchoolRegistrationReq;
import com.smartvehicle.payload.response.SchoolRegistrationResponse;
import com.smartvehicle.payload.response.SchoolResponseDTO;
import com.smartvehicle.repository.ParentRepository;
import com.smartvehicle.repository.SchoolRepository;
import com.smartvehicle.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/school")
public class SchoolController {


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
    private SchoolMapper schoolMapper;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<SchoolRegistrationResponse> registerSchool(@RequestBody SchoolRegistrationReq request) {
        System.out.println(request.getSchoolId());

        if (schoolRepository.existsById(request.getSchoolId())) {
            throw new RuntimeException("Error: A school with this Id already exists "+request.getSchoolId());
        }
        if (schoolRepository.existsByName(request.getName())) {
            throw new RuntimeException("Error: A school with this name already exists.");
        }
        // Create and save the School entity
        School school = new School();
        school.setId(request.getSchoolId());
        school.setName(request.getName());
        school.setCountryId(request.getCountryId());
        school.setProvId(request.getProvId());
        school.setAreaId(request.getAreaId());
        school.setEntityId(request.getEntityId());
        school.setContactName(request.getContactName());
        school.setContactNum(request.getContactNum());
        school.setStatus(request.getStatus());
        schoolRepository.save(school);

        // Return a response DTO
        SchoolRegistrationResponse response = new SchoolRegistrationResponse(school.getId(), school.getName(), school.getContactName(), school.getContactNum());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getSchool(@PathVariable(required = true) String id) {
        List<SchoolResponseDTO> schoolResponseDTOS=new ArrayList<>();
        if(StringUtils.hasText(id)){
            School school =schoolRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Error: School not found with id  "+id));
            SchoolResponseDTO schoolResponseDTO= schoolMapper.toResponseDTO(school);
            schoolResponseDTOS.add(schoolResponseDTO);

        }else {
            List<School> schools =schoolRepository.findAll();
            schoolResponseDTOS= schools.stream()
                    .map(s-> schoolMapper.toResponseDTO(s)).collect(Collectors.toList());
        }
        return ResponseEntity.ok(schoolResponseDTOS);
    }
    @GetMapping("")
    public ResponseEntity<?> getSchools() {
        List<SchoolResponseDTO> schoolResponseDTOS=new ArrayList<>();
            List<School> schools =schoolRepository.findAll();
            schoolResponseDTOS= schools.stream()
                    .map(s-> schoolMapper.toResponseDTO(s)).collect(Collectors.toList());

        return ResponseEntity.ok(schoolResponseDTOS);
    }

    @GetMapping("/admin/{adminId}")
    public ResponseEntity<?> getSchoolByAdminId(@PathVariable(required = true) Long adminId) {
        List<SchoolResponseDTO> schoolResponseDTOS=new ArrayList<>();

            List<School> schools =schoolRepository.findByAdmin_Id(adminId);
            schoolResponseDTOS= schools.stream()
                    .map(s-> schoolMapper.toResponseDTO(s)).collect(Collectors.toList());

        return ResponseEntity.ok(schoolResponseDTOS);
    }

    @PutMapping("/update")
    public ResponseEntity<SchoolResponseDTO> updateSchoolById(
            @RequestParam String id,
            @RequestBody SchoolRegistrationReq request) {

        // Find the school by ID
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found with ID: " + id));

        // Update only the fields provided in the request
        if (request.getName() != null) {
            school.setName(request.getName());
        }

        if (request.getCountryId() != null) {
            school.setCountryId(request.getCountryId());
        }

        if (request.getProvId() != null) {
            school.setProvId(request.getProvId());
        }

        if (request.getAreaId() != null) {
            school.setAreaId(request.getAreaId());
        }

        if (request.getEntityId() != null) {
            school.setEntityId(request.getEntityId());
        }

        if (request.getContactNum() != null) {
            school.setContactNum(request.getContactNum());
        }

        if (request.getContactName() != null) {
            school.setContactName(request.getContactName());
        }

        if (request.getStatus() != null) {
            school.setStatus(request.getStatus());
        }

        // Save the updated school
        School updatedSchool = schoolRepository.save(school);

        // Map to DTO using MapStruct
        SchoolResponseDTO responseDTO = schoolMapper.toResponseDTO(updatedSchool);

        return ResponseEntity.ok(responseDTO);
    }
}
