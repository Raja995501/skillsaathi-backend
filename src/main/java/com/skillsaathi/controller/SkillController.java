package com.skillsaathi.controller;

import com.skillsaathi.dto.common.ApiResponse;
import com.skillsaathi.dto.skill.*;
import com.skillsaathi.service.SkillService;
import com.skillsaathi.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @GetMapping("/skills/categories")
    public ResponseEntity<ApiResponse<List<SkillCategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(skillService.getAllCategories()));
    }

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getSkills(
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.success(skillService.getSkills(category, search)));
    }

    @GetMapping("/users/me/skills")
    public ResponseEntity<ApiResponse<List<UserSkillResponse>>> getMySkills() {
        List<UserSkillResponse> response = skillService.getMySkills(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/users/me/skills")
    public ResponseEntity<ApiResponse<UserSkillResponse>> addSkill(@Valid @RequestBody AddUserSkillRequest request) {
        UserSkillResponse response = skillService.addUserSkill(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Skill added", response));
    }

    @PutMapping("/users/me/skills/{id}")
    public ResponseEntity<ApiResponse<UserSkillResponse>> updateSkill(
            @PathVariable Long id, @Valid @RequestBody UpdateUserSkillRequest request) {
        UserSkillResponse response = skillService.updateUserSkill(SecurityUtils.getCurrentUserId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Skill updated", response));
    }

    @DeleteMapping("/users/me/skills/{id}")
    public ResponseEntity<ApiResponse<Void>> removeSkill(@PathVariable Long id) {
        skillService.removeUserSkill(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Skill removed", null));
    }
}
