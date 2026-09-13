package com.skillsaathi.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SubmitReportRequest {

    @NotNull
    private Long reportedUserId;

    @NotBlank
    @Size(max = 255)
    private String reason;

    @Size(max = 1000)
    private String description;
}
