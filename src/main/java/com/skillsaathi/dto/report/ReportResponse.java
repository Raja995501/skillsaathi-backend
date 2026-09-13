package com.skillsaathi.dto.report;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportResponse {
    private Long id;
    private Long reportedByUserId;
    private String reportedByName;
    private Long reportedUserId;
    private String reportedUserName;
    private String reason;
    private String description;
    private String status;
    private LocalDateTime createdAt;
}
