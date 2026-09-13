package com.skillsaathi.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ResolveReportRequest {

    @NotBlank
    private String status; // REVIEWED, DISMISSED
}
