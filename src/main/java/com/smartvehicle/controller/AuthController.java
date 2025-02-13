package com.smartvehicle.controller;

import com.smartvehicle.entity.*;
import com.smartvehicle.exception.ApplicationException;
import com.smartvehicle.payload.request.LoginRequest;
import com.smartvehicle.payload.request.ResetPasswordRequest;
import com.smartvehicle.payload.request.SignupRequest;
import com.smartvehicle.payload.response.*;
import com.smartvehicle.repository.*;
import com.smartvehicle.security.jwt.CustomRequestContextHolder;
import com.smartvehicle.security.jwt.JwtUtils;
import com.smartvehicle.security.services.TwilioVerificationService;
import com.smartvehicle.security.services.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class AuthController {
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
    @Autowired
    private ParentRepository parentRepository;
    @Autowired
    private DriverRepository driverRepository;
    @Autowired
    private AttenderRepository attenderRepository;
    @Autowired
    private AdminRepository adminRepository;
    /**
     * Authenticates the user with the provided login credentials.
     *
     * @param loginRequest The login request containing the username and password.
     * @return A ResponseEntity containing the JWT token and user details if authentication is successful.
     */
    @PostMapping("/signin")
    @Transactional
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUserName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(authentication);
//        List<String> roles = user.getRoles().stream().map(Role::getName)
//                .collect(Collectors.toList());
        if(user.getTwoFactorEnabled()){
            String status = twilioVerificationService.startVerification(userDetails.getPhone());
            if(status.equals("failed")){
                throw new RuntimeException("Failed verification service");
            }
        }
        try {
            if (StringUtils.hasText(CustomRequestContextHolder.getDeviceType())
                    && CustomRequestContextHolder.getDeviceType().equalsIgnoreCase(ClientType.MOBILE.name())
                    && StringUtils.hasText(CustomRequestContextHolder.getDeviceToken())) {
                log.debug("Saving device token for user {} ",loginRequest.getUserName());
                user.setDeviceToken(CustomRequestContextHolder.getDeviceToken());
                userRepository.save(user);
            }
        }catch (Exception e){
            log.error("Failed for saving device token for user {} ",e.getMessage());
        }
        UserEntityResDTO entityObj=new UserEntityResDTO();
            List<Role> roles =user.getRoles();
        List<String> roleNames = new ArrayList<>();
            if (!CollectionUtils.isEmpty(roles)) {
                Role role =roles.get(0);
                roleNames = roles.stream().map(r->r.getName()).toList();
                validateMobileLogin(role.getIsMobile());
                try {
                    String roleName = role.getName().toUpperCase();
                    switch (roleName) {
                        case "PARENT":
                            Parent parent = parentRepository.findByUser_Id(user.getId());
                            entityObj.setId(parent.getId());
                            entityObj.setFirstName(parent.getFirstName());
                            entityObj.setLastName(parent.getLastName());
                            break;
                        case "DRIVER":
                            Driver driver = driverRepository.findByUser_Id(user.getId()).get();
                            entityObj.setId(driver.getId());
                            entityObj.setFirstName(driver.getFirstName());
                            entityObj.setLastName(driver.getLastName());
                            break;
                        case "ATTENDER":
                            Attender attender = attenderRepository.findByUser_Id(user.getId()).get();
                            entityObj.setId(attender.getId());
                            entityObj.setFirstName(attender.getFirstName());
                            entityObj.setLastName(attender.getLastName());
                            break;
                        case "ADMIN":
                            Admin admin = adminRepository.findByUser_Id(user.getId()).get();
                            entityObj.setId(admin.getId());
                            entityObj.setFirstName(admin.getFirstName());
                            entityObj.setLastName(admin.getLastName());
                            break;
                        default:
                    }

                } catch (Exception e) {
                    log.error("failed to get entity id {} ", e.getMessage());
                }
            }else{
                throw new ApplicationException("No role assigned to this user");
            }
        return ResponseEntity
                .ok(new JwtResponse(jwt, userDetails.getId(),userDetails.getUsername(),user.getTwoFactorEnabled(),
                        roleNames,
                        entityObj));
    }

    private  void validateMobileLogin(boolean isMobile) {
        if (CustomRequestContextHolder.getDeviceType().equalsIgnoreCase(ClientType.MOBILE.name())){
            if(!isMobile){
                throw new ApplicationException("Mobile login not allowed for this user",HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @PostMapping("/resetpassword")
    public ResponseEntity<?> getStudents(Authentication authentication,
                                         @RequestBody ResetPasswordRequest request) {
        if (authentication == null || !(authentication.getDetails() instanceof Map)) {
            throw new RuntimeException("Unauthorised User");
        }

        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Long userId = (Long) details.get("userId");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Invalid Request : No such Username found"));
        user.setPassword(encoder.encode(request.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(true);
    }

//    @PostMapping("/signup")
//    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest request,
//                                          @RequestParam(required = true) String userType) throws Exception{
//        try {
//            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
//                return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!",null));
//            }
//            User user = new User();
//            user.setUsername(request.getUsername());
//            user.setPassword(encoder.encode(request.getPassword()));
//            user.setPhone(request.getPhone());
//            user.setEmail(request.getEmail());
//            user.setTwoFactorEnabled(false);
//            user.setStatus(true);
//            Set<Role> roles = new HashSet<>();
//            if(userType.toUpperCase().equalsIgnoreCase(ERole.ADMIN.name())){
//                    user.setTwoFactorEnabled(true);
//            }
//            Role role = roleRepository.findByName(userType.toUpperCase())
//                        .orElseThrow(() -> new RuntimeException("Error: No role find with userType  "+userType));
//            roles.add(role);
//            user.setRoles(roles);
//            userRepository.save(user);
//            switch (userType.toUpperCase()){
//                case "PARENT":
//                    Parent parent = new Parent();
//                    parent.setUser(user);
//                    break;
//                default:
//                    break;
//            }
//            SignupResponse response = new SignupResponse(request.getUsername(), request.getPhone());
//            return new ResponseEntity<SignupResponse>(response, HttpStatus.CREATED);
//        }
//        catch(Exception error){
//            return new ResponseEntity<ErrorResponse>(new ErrorResponse(error.getMessage(),HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
//        }
//    }
//    @PostMapping("/signup")
//    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest request) throws Exception{
//        try {
//            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
//                return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
//            }
//            User user = new User();
//            user.setUsername(request.getUsername());
//            user.setPassword(encoder.encode(request.getPassword()));
//            user.setPhone(request.getPhone());
//            user.setEmail(request.getEmail());
//            user.setTwoFactorEnabled(false);
//            user.setStatus(true);
//
//
//            Set<String> strRoles = request.getRole();
//            Set<Role> roles = new HashSet<>();
//
//            if (strRoles == null) {
//                throw new RuntimeException("Role not provided");
//            } else {
//                strRoles.forEach(role -> {
//                    switch (role) {
//                        case "PARENT":
//                            Role parentRole = roleRepository.findByName(ERole.PARENT)
//                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                            roles.add(parentRole);
//
//                            break;
//                        case "DRIVER":
//                            Role driverRole = roleRepository.findByName(ERole.DRIVER)
//                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                            roles.add(driverRole);
//
//                            break;
//                        case "ATTENDEE":
//                            Role attendeeRole = roleRepository.findByName(ERole.ATTENDEE)
//                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                            roles.add(attendeeRole);
//
//                            break;
//                        case "ADMIN":
//                            Role adminRole = roleRepository.findByName(ERole.ADMIN)
//                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//                            roles.add(adminRole);
//                            user.setTwoFactorEnabled(true);
//                            break;
//                        default:
//                            throw new RuntimeException("Invalid role: role could not be found");
//                    }
//                });
//            }
//            user.setRoles(roles);
//            User newUser = userRepository.save(user);
//            SignupResponse response = new SignupResponse(request.getUsername(), request.getPhone());
//            return new ResponseEntity<SignupResponse>(response, HttpStatus.CREATED);
//        }
//        catch(Exception error){
//            return new ResponseEntity<ErrorResponse>(new ErrorResponse(error.getMessage(),HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
//        }
//    }

}
