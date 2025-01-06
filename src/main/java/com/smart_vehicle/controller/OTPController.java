package com.smart_vehicle.controller;

import com.smart_vehicle.models.User;
import com.smart_vehicle.payload.request.VerifyOTPRequest;
import com.smart_vehicle.payload.response.ErrorResponse;
import com.smart_vehicle.payload.response.JwtResponse;
import com.smart_vehicle.repository.UserRepository;
import com.smart_vehicle.security.services.TwilioVerificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/otp")
public class OTPController {

    @Autowired
    UserRepository userRepository;
    @Autowired
    private TwilioVerificationService twilioVerificationService;

    @PostMapping("/verify")
    public ResponseEntity<?> authenticateOTP(@Valid @RequestBody VerifyOTPRequest request){
        try{
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("Invalid Request : No such Username found"));
            List<String> roles =user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList());
            String status = twilioVerificationService.checkVerification(user.getPhone(),request.getOtp());
            if(status.equals("approved")){
                return ResponseEntity
                        .ok(new JwtResponse(user.getToken(), user.getUserId(),
                                user.getUsername(),user.getTwoFactorEnabled(),roles));
            }

            return new ResponseEntity<ErrorResponse>(new ErrorResponse("User Unauthorised", HttpStatus.UNAUTHORIZED),HttpStatus.UNAUTHORIZED);

        }
        catch(Exception error){
            return new ResponseEntity<ErrorResponse>(new ErrorResponse(error.getMessage(),HttpStatus.UNAUTHORIZED),HttpStatus.UNAUTHORIZED);
        }
    }

}
