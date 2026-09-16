package com.skillsaathi.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PresenceEventResponse {
    @JsonProperty("userId")
    private Long userId;
    @JsonProperty("online")
    private boolean online;
}
