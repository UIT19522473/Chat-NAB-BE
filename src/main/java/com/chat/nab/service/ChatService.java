package com.chat.nab.service;

import com.chat.nab.dto.ChatHistoryResponse;
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

    /**
     * Phân trang lịch sử chat.
     * Redis list: index 0 = oldest, index N-1 = newest (do RPUSH).
     * page=0 → 20 tin mới nhất, page=1 → 20 tin cũ hơn, v.v.
     * Kết quả trả về theo thứ tự chronological (oldest → newest) trong mỗi page.
     */
    public ChatHistoryResponse getPagedMessages(String roomId, int page, int size) {
        String key = String.format(ROOM_KEY, roomId);
        Long totalSize = redisTemplate.opsForList().size(key);

        if (totalSize == null || totalSize == 0) {
            return new ChatHistoryResponse(List.of(), false);
        }

        // end = index của tin mới nhất trong page này
        long end = totalSize - 1 - ((long) page * size);
        if (end < 0) {
            return new ChatHistoryResponse(List.of(), false);
        }

        long startRaw = end - size + 1;
        long start = Math.max(0, startRaw);
        // hasMore = còn tin cũ hơn phía trước start
        boolean hasMore = startRaw > 0;

        List<String> jsonList = redisTemplate.opsForList().range(key, start, end);
        if (jsonList == null) {
            return new ChatHistoryResponse(List.of(), false);
        }

        List<ChatMessage> messages = jsonList.stream()
                .map(this::deserialize)
                .toList();

        return new ChatHistoryResponse(messages, hasMore);
    }

    private ChatMessage deserialize(String json) {
        try {
            return objectMapper.readValue(json, ChatMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize message", e);
        }
    }
}
