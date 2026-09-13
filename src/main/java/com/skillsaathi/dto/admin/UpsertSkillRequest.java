package com.skillsaathi.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpsertSkillRequest {

    @NotBlank
    private String name;

    @NotNull
    private Long categoryId;
}
