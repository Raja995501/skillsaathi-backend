package com.skillsaathi.controller;

import com.skillsaathi.dto.common.ApiResponse;
import com.skillsaathi.dto.user.PublicProfileResponse;
import com.skillsaathi.dto.user.UpdateProfileRequest;
import com.skillsaathi.dto.user.UserProfileResponse;
import com.skillsaathi.service.UserService;
import com.skillsaathi.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        UserProfileResponse response = userService.getMyProfile(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/users/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse response = userService.updateMyProfile(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", response));
    }

    @PostMapping(value = "/users/me/profile-picture", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> updateProfilePicture(@RequestParam("file") MultipartFile file) {
        String url = userService.updateProfilePicture(SecurityUtils.getCurrentUserId(), file);
        return ResponseEntity.ok(ApiResponse.success("Profile picture updated", url));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<PublicProfileResponse>> getPublicProfile(@PathVariable Long id) {
        PublicProfileResponse response = userService.getPublicProfile(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
