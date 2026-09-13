package com.skillsaathi.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UpdateProfileRequest {

    @Size(max = 100)
    private String name;

    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Invalid phone number")
    private String phone;

    private String gender;          // MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    private LocalDate dateOfBirth;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 1000)
    private String bio;

    private String languages;
    private String availability;       // WEEKDAYS, WEEKENDS, EVENINGS, FLEXIBLE
    private String onlinePreference;   // ONLINE, OFFLINE, BOTH
}
