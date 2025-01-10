package com.smartvehicle.controller;

import com.smartvehicle.Service.StudentService;
import com.smartvehicle.Service.UserService;
import com.smartvehicle.entity.*;
import com.smartvehicle.mapper.SchoolMapper;
import com.smartvehicle.payload.request.ParentSignupReq;
import com.smartvehicle.payload.response.ErrorResponse;
import com.smartvehicle.payload.response.SchoolResponseDTO;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

}
