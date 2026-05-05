package com.chat.nab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationMessage {
    private String fromUserId;
    private String roomId;
    private String content;
    private String createdAt;
    // "mention" | "mention_all"
    private String type;
}
