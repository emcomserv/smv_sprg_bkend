package com.smart_vehicle.controller;

import com.smart_vehicle.models.User;
import com.smart_vehicle.payload.request.LoginRequest;
import com.smart_vehicle.payload.request.SignupRequest;
import com.smart_vehicle.payload.response.JwtResponse;
import com.smart_vehicle.payload.response.MessageResponse;
import com.smart_vehicle.payload.response.SignupResponse;
import com.smart_vehicle.repository.UserRepository;
import com.smart_vehicle.security.jwt.JwtUtils;
import com.smart_vehicle.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    /**
     * Authenticates the user with the provided login credentials.
     *
     * @param loginRequest The login request containing the username and password.
     * @return A ResponseEntity containing the JWT token and user details if authentication is successful.
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity
                .ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername()));

    }

    /**
     * Registers a new user.
     *
     * @param signUpRequest the signup request containing user details
     * @return a ResponseEntity with a message response indicating the result of the registration
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        User user = new User(request.getUsername(),
                encoder.encode(request.getPassword()),request.getPhone());

        User newUser = userRepository.save(user);
        SignupResponse response = new SignupResponse(request.getUsername(), request.getPhone());

        return new ResponseEntity<SignupResponse>(response, HttpStatus.CREATED);

    }

}
