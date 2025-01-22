package com.smartvehicle.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    /**
     * Send a notification to a specific device using the device's FCM token.
     *
     * @param targetToken The FCM token of the target device.
     * @param title       The title of the notification.
     * @param body        The body of the notification.
     */
    public void sendNotification(String targetToken, String title, String body) {
        try {
            Message message = Message.builder()
                    .setToken(targetToken)
                    .putData("title", title)
                    .putData("body", body)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Notification sent successfully: " + response);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
}
