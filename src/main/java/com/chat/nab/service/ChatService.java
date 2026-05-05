package com.chat.nab.service;

import com.chat.nab.dto.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String ROOM_KEY = "chat:room:%s:messages";
    private static final long MAX_MESSAGES = 5000;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void saveMessage(ChatMessage message) {
        String key = String.format(ROOM_KEY, message.getRoomId());
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(key, json);
            redisTemplate.opsForList().trim(key, -MAX_MESSAGES, -1);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize message", e);
        }
    }

    public List<ChatMessage> getMessages(String roomId) {
        String key = String.format(ROOM_KEY, roomId);
        List<String> jsonList = redisTemplate.opsForList().range(key, 0, -1);
        if (jsonList == null) return List.of();
        return jsonList.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, ChatMessage.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to deserialize message", e);
                    }
                })
                .toList();
    }
}
