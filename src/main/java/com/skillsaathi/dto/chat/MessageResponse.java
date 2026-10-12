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
    private String status;

    // Media
    private String type;
    private String fileUrl;

    // Delete
    private Boolean deleted;
    private Boolean canDelete;
    private Boolean deletedForEveryone;

    // Edit
    private Boolean edited;
    private LocalDateTime editedAt;

    // Reply
    private Long replyToId;
    private String replyToContent;
    private String replyToSenderName;
    private String replyToType;

    private LocalDateTime createdAt;
}