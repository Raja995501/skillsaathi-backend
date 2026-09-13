package com.skillsaathi.dto.chat;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MessageResponse {
    private Long id;
    private Long connectionId;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String content;
    private String status;   // SENT, DELIVERED, READ
    private LocalDateTime createdAt;
}
