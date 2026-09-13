package com.skillsaathi.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpsertSkillCategoryRequest {

    @NotBlank
    private String name;
}
