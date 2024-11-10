package com.smart_vehicle.controller;

import com.smart_vehicle.models.Parent;
import com.smart_vehicle.payload.request.LoginRequest;
import com.smart_vehicle.payload.request.ParentSignupRequest;
import com.smart_vehicle.payload.response.JwtResponse;
import com.smart_vehicle.payload.response.MessageResponse;
import com.smart_vehicle.payload.response.ParentSignupResponse;
import com.smart_vehicle.repository.ParentRepository;
import com.smart_vehicle.security.services.OTPService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.smart_vehicle.security.jwt.JwtUtils;
import com.smart_vehicle.security.services.ParentDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth/parent")
public class ParentController {

    private static final Logger logger = LoggerFactory.getLogger(ParentController.class);
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    ParentRepository parentRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    OTPService otpService;
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println(loginRequest);
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        ParentDetailsImpl parentDetails = (ParentDetailsImpl) authentication.getPrincipal();

        return ResponseEntity
                .ok(new JwtResponse(jwt, parentDetails.getId(), parentDetails.getUsername(), parentDetails.getUserEmail()));

    }

    @GetMapping("/test")
    public String test(){
        String s = "dfdf";
        String b = "dfdfd";
        return "working";
    }
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody ParentSignupRequest request) {
        System.out.println(request);
        if (parentRepository.findByUserName(request.getUserName()).isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (parentRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }
        System.out.println("validated parent..........");
        // Create new user's account
        Parent parent = new Parent(request.getParentName(),request.getUserName(), request.getEmail(),
                encoder.encode(request.getPassword()),request.getContactNum());
        //    user.setCreatedBy(signUpRequest.getUsername());
        //    user.setCreatedDate(new Date());
        System.out.println(parent);
        Parent newParent = parentRepository.save(parent);
        ParentSignupResponse response = new ParentSignupResponse(request.getEmail(), request.getContactNum());
//		boolean emailOTP = otpService.sendOTPToEmail(request.getEmail());
//        response.setEmailOTP(emailOTP);

        return new ResponseEntity<ParentSignupResponse>(response, HttpStatus.CREATED);

    }
}
