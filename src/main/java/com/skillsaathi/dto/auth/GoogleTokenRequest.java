package com.skillsaathi.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleTokenRequest {

    @NotBlank
    private String token;
}