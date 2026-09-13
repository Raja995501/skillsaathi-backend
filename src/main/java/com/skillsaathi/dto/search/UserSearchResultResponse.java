package com.skillsaathi.dto.search;

import com.skillsaathi.dto.skill.UserSkillResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserSearchResultResponse {
    private Long id;
    private String name;
    private String profilePictureUrl;
    private String city;
    private String state;
    private String onlinePreference;
    private BigDecimal averageRating;
    private List<UserSkillResponse> skillsToTeach;
    private List<UserSkillResponse> skillsToLearn;

    /** Only populated when the caller is authenticated and sort=matchScore was requested. */
    private BigDecimal matchPercentage;
}
