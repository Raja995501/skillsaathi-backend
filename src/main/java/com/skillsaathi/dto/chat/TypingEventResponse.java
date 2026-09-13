package com.skillsaathi.dto.chat;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TypingEventResponse {
    private Long connectionId;
    private Long userId;
    private String userName;
    private boolean typing;
}
