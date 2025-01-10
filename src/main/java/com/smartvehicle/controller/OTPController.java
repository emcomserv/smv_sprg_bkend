package com.smartvehicle.controller;

import com.smartvehicle.entity.User;
import com.smartvehicle.payload.request.VerifyOTPRequest;
import com.smartvehicle.payload.response.ErrorResponse;
import com.smartvehicle.repository.UserRepository;
import com.smartvehicle.security.services.TwilioVerificationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/otp")
@Slf4j
public class OTPController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    private TwilioVerificationService twilioVerificationService;

    @PostMapping("/verify")
    public ResponseEntity<?> authenticateOTP(@Valid @RequestBody VerifyOTPRequest request) {
        System.out.println("OTP "+request.getOtp()+" User name "+request.getUsername());

        try {
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Invalid Request : No such Username found"));
            String status = twilioVerificationService.checkVerification(user.getPhone(), request.getOtp());
            if (status.equals("approved")) {
                return ResponseEntity
                        .ok(status);
            }
            return new ResponseEntity<ErrorResponse>(new ErrorResponse("OTP validation failed!",
                    HttpStatus.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
        } catch (Exception error) {
            log.error(error.getMessage());
            return new ResponseEntity<ErrorResponse>(new ErrorResponse(error.getMessage(),
                    HttpStatus.UNAUTHORIZED), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/resend")
    public String resendOTP(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof Map)) {
            throw new RuntimeException("Unauthorised User");
        }
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        Long userId = (Long) details.get("userId");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Invalid Request : No such Username found"));
        return twilioVerificationService.startVerification(user.getPhone());

    }

}
