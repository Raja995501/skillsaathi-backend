package com.skillsaathi.dto.review;

import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewResponse {
    private Long id;
    private Long connectionId;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerProfilePicture;
    private Long revieweeId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
