package com.skillsaathi.dto.user;

import com.skillsaathi.dto.skill.UserSkillResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Full profile — returned only to the profile owner (/users/me). */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String profilePictureUrl;
    private String gender;
    private LocalDate dateOfBirth;
    private String city;
    private String state;
    private String bio;
    private String languages;
    private String availability;
    private String onlinePreference;
    private boolean emailVerified;
    private BigDecimal averageRating;
    private List<UserSkillResponse> skillsToTeach;
    private List<UserSkillResponse> skillsToLearn;
}
