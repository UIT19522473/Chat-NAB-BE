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
    private static final Duration TTL = Duration.ofMinutes(10);

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
    // public boolean claimUser(String userId) {
    //     if (!ALL_USERS.contains(userId)) return false;
    //     Boolean set = redisTemplate.opsForValue()
    //             .setIfAbsent(String.format(ONLINE_KEY, userId), "online", TTL);
    //     return Boolean.TRUE.equals(set);
    // }

    // Cho phép connect thoải mái, mỗi lần gọi là một lần làm mới (refresh) trạng thái online
    public boolean claimUser(String userId) {
        if (!ALL_USERS.contains(userId)) return false;
        
        String key = String.format(ONLINE_KEY, userId);
        // Luôn ghi đè giá trị "online", không quan tâm trước đó có ai chưa
        redisTemplate.opsForValue().set(key, "online", TTL);
        
        return true;
    }
    
    // Thêm hàm này để khi người dùng nhấn Logout thì xóa ngay lập tức
    public void releaseUser(String userId) {
        redisTemplate.delete(String.format(ONLINE_KEY, userId));
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

    // Logic giải phóng user
    public void releaseUser(String userId) {
        redisTemplate.delete(String.format(ONLINE_KEY, userId));
    }
}
