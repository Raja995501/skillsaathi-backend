package com.skillsaathi.dto.chat;

import com.skillsaathi.entity.enums.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChatMessageRequest {

    @NotNull
    private Long connectionId;

    private String content;

    private String fileUrl;

    @Builder.Default
    private MessageType type = MessageType.TEXT;

    // ✅ Reply
    private Long replyToId;

    // Optional
    private Long senderId;
}