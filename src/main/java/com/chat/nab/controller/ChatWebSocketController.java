package com.chat.nab.controller;

import com.chat.nab.dto.ChatMessage;
import com.chat.nab.dto.NotificationMessage;
import com.chat.nab.dto.TypingEvent;
import com.chat.nab.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    @MessageMapping("/rooms/{roomId}/send")
    public void sendMessage(@DestinationVariable String roomId,
                            @Payload ChatMessage message) {
        if (message.getUserId() == null || message.getUserId().isBlank()) return;
        if (message.getContent() == null || message.getContent().isBlank()) return;

        message.setId(UUID.randomUUID().toString());
        message.setRoomId(roomId);
        message.setCreatedAt(Instant.now().toString());

        chatService.saveMessage(message);
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, message);

        processMentions(message);
    }

    @MessageMapping("/rooms/{roomId}/typing")
    public void handleTyping(@DestinationVariable String roomId,
                             @Payload TypingEvent event) {
        event.setRoomId(roomId);
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId + "/typing", event);
    }

    private void processMentions(ChatMessage message) {
        Matcher matcher = MENTION_PATTERN.matcher(message.getContent());

        NotificationMessage notification = NotificationMessage.builder()
                .fromUserId(message.getUserId())
                .roomId(message.getRoomId())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();

        Set<String> mentioned = new HashSet<>();
        boolean mentionAll = false;

        while (matcher.find()) {
            String target = matcher.group(1);
            if ("all".equalsIgnoreCase(target)) {
                mentionAll = true;
            } else {
                mentioned.add(target);
            }
        }

        if (mentionAll) {
            notification.setType("mention_all");
            messagingTemplate.convertAndSend(
                    "/topic/rooms/" + message.getRoomId() + "/notifications", notification);
        }

        // Gửi notification cá nhân, bỏ qua chính người gửi
        for (String targetUserId : mentioned) {
            if (targetUserId.equals(message.getUserId())) continue;
            notification.setType("mention");
            messagingTemplate.convertAndSend(
                    "/topic/users/" + targetUserId + "/notifications", notification);
        }
    }
}
