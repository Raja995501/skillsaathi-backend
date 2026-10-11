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

    // === MEDIA (IMAGE/VIDEO) ===
    private String type;     // TEXT, IMAGE, VIDEO
    private String fileUrl;

    // === DELETE FEATURE ===
    private Boolean deleted;        // true if deleted for everyone OR for current user
    private Boolean canDelete;      // true if current user can delete this message
    private Boolean deletedForEveryone;  // true if deleted for everyone (shows "This message was deleted")
    // ========================

    private LocalDateTime createdAt;
}