package com.skillsaathi.dto.chat;

import com.skillsaathi.entity.enums.MessageType; // === ADDED FOR MEDIA ===
import jakarta.validation.constraints.NotNull;
import lombok.*;

/** Inbound payload for STOMP destination /app/chat.send */
@Builder
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChatMessageRequest {

    @NotNull
    private Long connectionId;

    private String content; // Text message (optional agar image/video bheji ja rahi ho)

    // === ADDED FOR MEDIA (IMAGE/VIDEO) ===
    private String fileUrl; // Cloudinary secure URL

    @Builder.Default
    private MessageType type = MessageType.TEXT; // TEXT, IMAGE, VIDEO
    // =====================================

    // === ADDED SENDER ID SUPPORT ===
    private Long senderId;
}