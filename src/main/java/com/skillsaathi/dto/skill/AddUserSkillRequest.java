package com.skillsaathi.dto.skill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AddUserSkillRequest {

    @NotNull(message = "skillId is required")
    private Long skillId;

    @NotBlank(message = "type is required")
    private String type;   // TEACH, LEARN

    private String level;  // optional, applies mainly to TEACH
}
