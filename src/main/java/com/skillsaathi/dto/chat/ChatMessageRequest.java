package com.skillsaathi.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/** Inbound payload for STOMP destination /app/chat.send */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChatMessageRequest {

    @NotNull
    private Long connectionId;

    @NotBlank
    private String content;
}
