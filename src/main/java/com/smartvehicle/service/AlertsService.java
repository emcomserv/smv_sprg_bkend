package com.smartvehicle.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.smartvehicle.entity.Alert;
import com.smartvehicle.entity.User;
import com.smartvehicle.payload.request.AlertReq;
import com.smartvehicle.repository.AlertRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class AlertsService {

    @Autowired
    private AlertRepository alertRepository;

    /**
     * Send an alert and save it in the database.
     */
    public Alert sendAndSaveAlert(User user, AlertReq alertReq , User fromUser) {
        Alert alert = null;
        try {
            log.debug("Saving alert");
            alert = new Alert();
            alert.setUser(user);
            alert.setTitle(alertReq.getTitle());
            alert.setBody(alertReq.getBody());
            alert.setAlertType(alertReq.getAlertType());
            alertRepository.save(alert);
            Notification notification = Notification.builder()
                    .setTitle(alert.getTitle()) /// from user name
                    .setBody(alert.getBody())  //
                    .build();
            log.debug(" Sending notification via Firebase to User {} ",user.getId());
            Message message = Message.builder()
                    .setToken(user.getDeviceToken())
                    .setNotification(notification)
                    .putData("title", alert.getTitle())
                    .putData("body", alert.getBody())
                    .putData("alertType", alert.getAlertType())
                    .putData("fromUserId",""+fromUser.getId())
                    .putData("fromUserPhone", fromUser.getPhone())
                    .build();

//            FirebaseMessaging.getInstance().send(message);
            String response = sendAndGetResponse(message);
            System.out.println("Alert sent successfully! "+response);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send alert: " + e.getMessage());
        }
        return alert;
    }

    private String sendAndGetResponse(Message message) throws InterruptedException, ExecutionException {
        return FirebaseMessaging.getInstance().sendAsync(message).get();
    }
    public Alert update(User user, AlertReq alertReq) {
        Alert alert = null;

            // Save alert in DB
            alert = new Alert();
            alert.setUser(user);
            alert.setTitle(alertReq.getTitle());
            alert.setBody(alertReq.getBody());
            alert.setAlertType(alertReq.getAlertType());
            alertRepository.save(alert);

            System.out.println("Alert successfully updated !");

        return alert;
    }
    public List<Alert> getAlertsByUserId(Long userId) {
        return alertRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    public Alert getAlertsById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Alert not found with id  "+id));
    }
}

