package com.skillsaathi.dto.match;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MatchResponse {
    private Long userId;
    private String name;
    private String profilePictureUrl;
    private String city;
    private String state;
    private BigDecimal averageRating;

    private BigDecimal matchPercentage;
    private String matchReason;

    private List<String> theyTeachThatYouWantToLearn;
    private List<String> theyWantToLearnThatYouTeach;
}
