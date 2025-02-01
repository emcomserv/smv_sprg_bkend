package com.smartvehicle.controller;

import com.smartvehicle.service.AlertsService;
import com.smartvehicle.entity.Alert;
import com.smartvehicle.entity.User;
import com.smartvehicle.payload.request.AlertReq;
import com.smartvehicle.payload.response.AlertResponseDTO;
import com.smartvehicle.repository.UserRepository;
import com.smartvehicle.security.jwt.CustomRequestContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alert")
@Slf4j
public class AlertsController {

    @Autowired
    private AlertsService alertsService;
    @Autowired
    private UserRepository userRepository;
    @PostMapping("/send")
    public AlertResponseDTO sendAlert(@RequestBody AlertReq alertReq) {
        log.debug(" sendAlert ");
        Long userId = CustomRequestContextHolder.getUserId();
        log.debug(" sendAlert from user id "+userId);
        User toUser = userRepository.findById(alertReq.getUserId())
                .orElseThrow(() -> new RuntimeException("Error: User not found with id  "+alertReq.getUserId()));
        User fromUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Error: User not found with id  "+userId));
        Alert alert = alertsService.sendAndSaveAlert(toUser, alertReq,fromUser);
        return new AlertResponseDTO(alert.getId(),alertReq.getUserId());
    }
    @PostMapping("/read/{id}")
    public AlertResponseDTO read(@PathVariable Long id) {

        Alert alert = alertsService.getAlertsById(id);
        return new AlertResponseDTO(alert.getId(),alert.getUser().getId());
    }
    @GetMapping("/user/{userId}")
    public List<Alert> getAlerts(@PathVariable Long userId) {
        return alertsService.getAlertsByUserId(userId);
    }
}
