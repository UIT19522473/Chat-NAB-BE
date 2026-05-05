package com.chat.nab.service;

import com.chat.nab.dto.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String ONLINE_KEY  = "chat:online:%s";
    private static final String SESSION_KEY = "chat:session:%s";
    private static final Duration TTL = Duration.ofHours(1);

    private static final List<String> ALL_USERS = List.of(
            "TUANNN2", "HUNGND4", "LAMBMT", "LINHTT1", "TINHNV2"
    );

    private final StringRedisTemplate redisTemplate;

    public List<UserInfo> getAllUsersWithStatus() {
        return ALL_USERS.stream()
                .map(id -> new UserInfo(id,
                        Boolean.TRUE.equals(redisTemplate.hasKey(String.format(ONLINE_KEY, id)))))
                .toList();
    }

    // Atomic claim: trả true nếu claim thành công, false nếu đã bị chiếm
    public boolean claimUser(String userId) {
        if (!ALL_USERS.contains(userId)) return false;
        Boolean set = redisTemplate.opsForValue()
                .setIfAbsent(String.format(ONLINE_KEY, userId), "online", TTL);
        return Boolean.TRUE.equals(set);
    }

    // Lưu sessionId → userId khi WebSocket connect
    public void bindSession(String sessionId, String userId) {
        redisTemplate.opsForValue().set(
                String.format(SESSION_KEY, sessionId), userId, TTL);
    }

    // Xóa claim và session khi WebSocket disconnect
    public void releaseBySession(String sessionId) {
        String sessionKey = String.format(SESSION_KEY, sessionId);
        String userId = redisTemplate.opsForValue().get(sessionKey);
        if (userId != null) {
            redisTemplate.delete(String.format(ONLINE_KEY, userId));
            redisTemplate.delete(sessionKey);
        }
    }
}
