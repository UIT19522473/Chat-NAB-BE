package com.chat.nab.controller;

import com.chat.nab.dto.UserInfo;
import com.chat.nab.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserInfo> getUsers() {
        return userService.getAllUsersWithStatus();
    }

    @PostMapping("/claim")
    public ResponseEntity<String> claimUser(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("userId is required");
        }
        boolean claimed = userService.claimUser(userId);
        if (!claimed) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User đang được sử dụng");
        }
        return ResponseEntity.ok("OK");
    }

    // API mới: Giải phóng 1 userId cụ thể bằng phương thức GET
    @GetMapping("/release/{userId}")
    public ResponseEntity<String> releaseUser(@PathVariable String userId) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest().body("userId is required");
        }
        
        userService.releaseUser(userId);
        return ResponseEntity.ok("Đã giải phóng user: " + userId);
    }
}
