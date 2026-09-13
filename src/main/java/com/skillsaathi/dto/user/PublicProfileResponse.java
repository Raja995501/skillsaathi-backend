package com.skillsaathi.dto.user;

import com.skillsaathi.dto.skill.UserSkillResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/** Public-facing profile — what another user sees, no email/phone exposed. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PublicProfileResponse {
    private Long id;
    private String name;
    private String profilePictureUrl;
    private String city;
    private String state;
    private String bio;
    private String languages;
    private String availability;
    private String onlinePreference;
    private BigDecimal averageRating;
    private List<UserSkillResponse> skillsToTeach;
    private List<UserSkillResponse> skillsToLearn;
}
