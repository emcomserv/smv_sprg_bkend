package com.smart_vehicle.controller;

import com.smart_vehicle.models.Role;
import com.smart_vehicle.models.User;
import com.smart_vehicle.models.ERole;
import com.smart_vehicle.payload.request.LoginRequest;
import com.smart_vehicle.payload.request.SignupRequest;
import com.smart_vehicle.payload.request.VerifyOTPRequest;
import com.smart_vehicle.payload.response.ErrorResponse;
import com.smart_vehicle.payload.response.JwtResponse;
import com.smart_vehicle.payload.response.MessageResponse;
import com.smart_vehicle.payload.response.SignupResponse;
import com.smart_vehicle.repository.RoleRepository;
import com.smart_vehicle.repository.UserRepository;
import com.smart_vehicle.security.jwt.JwtUtils;
import com.smart_vehicle.security.services.TwilioVerificationService;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    private TwilioVerificationService twilioVerificationService;

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
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(authentication);
        List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
                .collect(Collectors.toList());
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setToken(jwt);
        userRepository.save(user);
        Boolean twoFactorAuthentication = false;
        if(roles.contains(ERole.ADMIN.name())){
            String status = twilioVerificationService.startVerification(userDetails.getPhone());
            if(status.equals("failed")){
                throw new RuntimeException("Failed verification service");
            }
            twoFactorAuthentication = true;
            return ResponseEntity
                    .ok(new JwtResponse(userDetails.getId(), userDetails.getUsername(),twoFactorAuthentication));
        }
        else{
            return ResponseEntity
                    .ok(new JwtResponse(jwt, userDetails.getId(),userDetails.getUsername(),twoFactorAuthentication));
        }

    }

    /**
     * Registers a new user.
     *
     * @param registerUser the signup request containing user details
     * @return a ResponseEntity with a message response indicating the result of the registration
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest request) throws Exception{
        try {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
            }
            User user = new User(request.getUsername(),
                    encoder.encode(request.getPassword()), request.getPhone());

            Set<String> strRoles = request.getRole();
            Set<Role> roles = new HashSet<>();

            if (strRoles == null) {
                throw new RuntimeException("Role not provided");
            } else {
                strRoles.forEach(role -> {
                    switch (role) {
                        case "PARENT":
                            Role parentRole = roleRepository.findByName(ERole.PARENT)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(parentRole);

                            break;
                        case "DRIVER":
                            Role driverRole = roleRepository.findByName(ERole.DRIVER)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(driverRole);

                            break;
                        case "ATTENDEE":
                            Role attendeeRole = roleRepository.findByName(ERole.ATTENDEE)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(attendeeRole);

                            break;
                        case "ADMIN":
                            Role adminRole = roleRepository.findByName(ERole.ADMIN)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(adminRole);
                            user.setTwoFactorEnabled(true);
                            break;
                        default:
                            throw new RuntimeException("Invalid role: role could not be found");
                    }
                });
            }
            user.setRoles(roles);
            User newUser = userRepository.save(user);
            SignupResponse response = new SignupResponse(request.getUsername(), request.getPhone());
            return new ResponseEntity<SignupResponse>(response, HttpStatus.CREATED);
        }
        catch(Exception error){
            return new ResponseEntity<ErrorResponse>(new ErrorResponse(error.getMessage(),HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/verifyOTP")
    public ResponseEntity<?> authenticateOTP(@Valid @RequestBody VerifyOTPRequest request){
       try{
           User user = userRepository.findByUsername(request.getUsername())
                   .orElseThrow(() -> new RuntimeException("Invalid Request : No such Username found"));

           String status = twilioVerificationService.checkVerification(user.getPhone(),request.getOtp());
           if(status.equals("approved")){
               return ResponseEntity
                       .ok(new JwtResponse(user.getToken(), user.getUserId(), user.getUsername(),user.getTwoFactorEnabled()));
           }

           return new ResponseEntity<ErrorResponse>(new ErrorResponse("User Unauthorised",HttpStatus.UNAUTHORIZED),HttpStatus.UNAUTHORIZED);

       }
       catch(Exception error){
           return new ResponseEntity<ErrorResponse>(new ErrorResponse(error.getMessage(),HttpStatus.UNAUTHORIZED),HttpStatus.UNAUTHORIZED);
       }
    }
}
