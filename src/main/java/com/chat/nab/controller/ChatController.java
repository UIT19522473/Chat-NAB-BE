package com.chat.nab.controller;

import com.chat.nab.dto.ChatHistoryResponse;
import com.chat.nab.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{roomId}/messages")
    public ChatHistoryResponse getMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return chatService.getPagedMessages(roomId, page, size);
    }
}
