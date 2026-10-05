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

    // === ADDED FOR MEDIA (IMAGE/VIDEO) ===
    private String type;     // TEXT, IMAGE, VIDEO
    private String fileUrl;  // Cloudinary secure URL
    // =====================================

    private LocalDateTime createdAt;
}