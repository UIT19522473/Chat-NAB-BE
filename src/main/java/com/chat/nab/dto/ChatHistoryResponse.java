package com.chat.nab.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatHistoryResponse {
    private List<ChatMessage> messages;
    private boolean hasMore;
}
