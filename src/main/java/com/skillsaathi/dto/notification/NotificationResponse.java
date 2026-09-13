package com.skillsaathi.dto.notification;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String body;
    private Long referenceId;
    private boolean read;
    private LocalDateTime createdAt;
}
