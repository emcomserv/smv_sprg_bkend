package com.smartvehicle.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
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
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(AndroidNotification.builder()
                            .setChannelId("default")
                            .setSound("default")
                            .build())
                    .build();

            ApnsConfig apnsConfig = ApnsConfig.builder()
                    .setAps(Aps.builder()
                            .setSound("default")
                            .build())
                    .build();

            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(notification) // ensures OS-rendered notification
                    .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                    .putData("title", title)
                    .putData("body", body)
                    .setAndroidConfig(androidConfig)
                    .setApnsConfig(apnsConfig)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Notification sent successfully: " + response);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to send notification: " + e.getMessage());
        }
    }
}
