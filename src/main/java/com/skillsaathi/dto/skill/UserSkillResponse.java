package com.skillsaathi.dto.skill;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserSkillResponse {
    private Long id;          // user_skill row id (needed for edit/delete)
    private Long skillId;
    private String skillName;
    private String categoryName;
    private String type;      // TEACH, LEARN
    private String level;     // BEGINNER..EXPERT (relevant for TEACH)
}
