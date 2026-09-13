package com.skillsaathi.dto.skill;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateUserSkillRequest {
    private String level;  // only level is editable; to change type/skill, delete + re-add
}
