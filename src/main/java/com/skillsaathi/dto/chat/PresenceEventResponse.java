package com.skillsaathi.dto.chat;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PresenceEventResponse {
    private Long userId;
    private boolean online;
}
