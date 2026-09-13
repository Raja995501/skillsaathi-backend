package com.skillsaathi.dto.chat;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/** Inbound payload for STOMP destination /app/chat.typing */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TypingEventRequest {

    @NotNull
    private Long connectionId;

    private boolean typing;
}
