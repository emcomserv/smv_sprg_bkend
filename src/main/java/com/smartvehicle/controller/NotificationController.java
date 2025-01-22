package com.smartvehicle.controller;

import com.smartvehicle.Service.NotificationService;
import com.smartvehicle.payload.request.NotificationReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;


    @PostMapping
    public String sendNotification(@RequestBody NotificationReq request) {
        notificationService.sendNotification(request.getTargetToken(), request.getTitle(), request.getBody());
        return "Notification sent!";
    }
}
