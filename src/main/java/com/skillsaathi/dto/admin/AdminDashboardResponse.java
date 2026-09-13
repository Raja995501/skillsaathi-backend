package com.skillsaathi.dto.admin;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminDashboardResponse {
    private long totalUsers;
    private long totalSkills;
    private long totalConnections;
    private long totalMessages;
    private long totalReviews;
    private long openReports;
}
