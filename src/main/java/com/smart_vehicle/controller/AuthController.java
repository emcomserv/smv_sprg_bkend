package com.smart_vehicle.controller;

import com.smart_vehicle.models.Role;
import com.smart_vehicle.models.User;
import com.smart_vehicle.models.ERole;
import com.smart_vehicle.payload.request.LoginRequest;
import com.smart_vehicle.payload.request.SignupRequest;
import com.smart_vehicle.payload.response.ErrorResponse;
import com.smart_vehicle.payload.response.JwtResponse;
import com.smart_vehicle.payload.response.MessageResponse;
import com.smart_vehicle.payload.response.SignupResponse;
import com.smart_vehicle.repository.RoleRepository;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
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



    /**
     * Authenticates the user with the provided login credentials.
     *
     * @param loginRequest The login request containing the username and password.
     * @return A ResponseEntity containing the JWT token and user details if authentication is successful.
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        User user = userRepository.findByUsername(loginRequest.getUserName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(authentication);
        List<String> roles = user.getRoles().stream().map(item -> item.getName().name())
                .collect(Collectors.toList());

//        user.setToken(jwt);
//        userRepository.save(user);
        Boolean twoFactorAuthentication = false;
        if(roles.contains(ERole.ADMIN.name())){
            twoFactorAuthentication = true;
        }

        return ResponseEntity
                .ok(new JwtResponse(jwt, userDetails.getId(),userDetails.getUsername(),twoFactorAuthentication,roles));
    }


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


}
