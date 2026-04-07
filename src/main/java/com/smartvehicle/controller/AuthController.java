package com.smartvehicle.controller;

import com.smartvehicle.entity.*;
import com.smartvehicle.exception.ApplicationException;
import com.smartvehicle.payload.request.LoginRequest;
import com.smartvehicle.payload.request.ResetPasswordRequest;
import com.smartvehicle.payload.response.JwtResponse;
import com.smartvehicle.payload.response.TokenRefreshResponse;
import com.smartvehicle.payload.request.TokenRefreshRequest;
import com.smartvehicle.payload.response.UserEntityResDTO;
import com.smartvehicle.repository.*;
import com.smartvehicle.security.jwt.CustomRequestContextHolder;
import com.smartvehicle.security.jwt.JwtUtils;
import com.smartvehicle.security.services.TwilioVerificationService;
import com.smartvehicle.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @PostMapping("/signin")
    @Transactional
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUserName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Authenticate username/password
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword()));

        // 2. If 2FA is enabled, trigger OTP and return OTP_REQUIRED (do NOT return JWT)
        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            String status = twilioVerificationService.startVerification(user.getPhone());
            if ("failed".equals(status)) {
                throw new RuntimeException("Failed verification service");
            }
            // Return a special response indicating OTP is required
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of(
                            "message", "OTP_REQUIRED",
                            "username", user.getUsername()
                    ));
        }

        // 3. If 2FA is not enabled, proceed as normal
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(authentication);
        String refreshToken = jwtUtils.generateRefreshToken(authentication);

        try {
            if (StringUtils.hasText(CustomRequestContextHolder.getDeviceType())
                    && CustomRequestContextHolder.getDeviceType().equalsIgnoreCase(ClientType.MOBILE.name())
                    && StringUtils.hasText(CustomRequestContextHolder.getDeviceToken())) {
                log.debug("Saving device token for user {}", loginRequest.getUserName());
                user.setDeviceToken(CustomRequestContextHolder.getDeviceToken());
                userRepository.save(user);
            }
        } catch (Exception e) {
            log.error("Failed for saving device token for user {} ", e.getMessage());
        }

        UserEntityResDTO entityObj = new UserEntityResDTO();
        List<Role> roles = user.getRoles();
        List<String> roleNames = new ArrayList<>();

        if (!CollectionUtils.isEmpty(roles)) {
            Role role = roles.get(0);
            roleNames = roles.stream().map(Role::getName).toList();
            validateMobileLogin(role.getIsMobile());

            try {
                switch (role.getName().toUpperCase()) {
                    case "PARENT":
                        Parent parent = parentRepository.findByUser_Id(user.getId());
                        entityObj.setId(parent.getId());
                        entityObj.setFirstName(parent.getFirstName());
                        entityObj.setLastName(parent.getLastName());
                        if (parent.getSchool() != null) {
                            entityObj.setSchoolId(parent.getSchool().getId());
                            entityObj.setSchoolName(parent.getSchool().getName());
                        }
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
                        if (admin.getSchool() != null) {
                            entityObj.setSchoolId(admin.getSchool().getId());
                            entityObj.setSchoolName(admin.getSchool().getName());
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                log.error("Failed to get entity id {}", e.getMessage());
            }
        } else {
            throw new ApplicationException("No role assigned to this user");
        }

        return ResponseEntity.ok(new JwtResponse(jwt, refreshToken, userDetails.getId(), userDetails.getUsername(), user.getTwoFactorEnabled(), roleNames, entityObj));
    }

    private void validateMobileLogin(boolean isMobile) {
        if (CustomRequestContextHolder.getDeviceType().equalsIgnoreCase(ClientType.MOBILE.name())) {
            if (!isMobile) {
                throw new ApplicationException("Mobile login not allowed for this user", HttpStatus.UNAUTHORIZED);
            }
        }
    }

    @PostMapping("/resetpassword")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        String userName = request.getUserName();
        Optional<User> optionalUser = userRepository.findByUsername(userName);
        if (!optionalUser.isPresent()) {
            throw new RuntimeException("Invalid Request : No such Username found");
        }
        User user = optionalUser.get();
        user.setPassword(encoder.encode(request.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        if (jwtUtils.validateJwtToken(requestRefreshToken)) {
            String username = jwtUtils.getUserNameFromJwtToken(requestRefreshToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ApplicationException("User not found!", HttpStatus.NOT_FOUND));

            String token = jwtUtils.generateTokenFromUser(user);
            String newRefreshToken = jwtUtils.generateRefreshTokenFromUser(user);

            return ResponseEntity.ok(new TokenRefreshResponse(token, newRefreshToken));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(java.util.Map.of("message", "Invalid or Expired Refresh Token"));
    }

    // 🔸 NEW: Send OTP manually for testing
    @GetMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String phone) {
        String result = twilioVerificationService.startVerification(phone);
        return ResponseEntity.ok("OTP Status: " + result);
    }

    // 🔸 NEW: Verify OTP manually for testing
    @GetMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String phone, @RequestParam String code) {
        String result = twilioVerificationService.checkVerification(phone, code);
        return ResponseEntity.ok("Verification Result: " + result);
    }
}
