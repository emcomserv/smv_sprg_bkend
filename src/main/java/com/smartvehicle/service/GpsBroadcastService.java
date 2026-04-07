package com.smartvehicle.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GpsBroadcastService {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastGpsData(String json) {
        messagingTemplate.convertAndSend("/topic/gps", json);
    }
}