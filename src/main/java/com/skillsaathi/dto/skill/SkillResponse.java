package com.skillsaathi.dto.skill;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkillResponse {
    private Long id;
    private String name;
    private Long categoryId;
    private String categoryName;
}
