package com.skillsaathi.dto.admin;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminUserSummaryResponse {
    private Long id;
    private String name;
    private String email;
    private String city;
    private String state;
    private boolean blocked;
    private boolean emailVerified;
    private BigDecimal averageRating;
    private LocalDateTime createdAt;
}
