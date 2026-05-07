package com.chat.nab.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class FcmService {

    public void sendPushNotification(List<String> tokens, String title, String body, Map<String, String> data) {
        if (FirebaseApp.getApps().isEmpty()) return;
        if (tokens == null || tokens.isEmpty()) return;

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data != null ? data : Map.of())
                .build();

        try {
            FirebaseMessaging.getInstance().sendEachForMulticast(message);
        } catch (FirebaseMessagingException e) {
            log.error("FCM send failed: {}", e.getMessage());
        }
    }
}
