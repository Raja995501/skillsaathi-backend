package com.skillsaathi.dto.connection;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConnectionResponse {
    private Long id;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserProfilePicture;
    private String direction;          // OUTGOING (I sent it) or INCOMING (I received it)
    private String status;             // PENDING, ACCEPTED, REJECTED, CANCELLED, BLOCKED
    private BigDecimal matchPercentage;
    private String matchReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
