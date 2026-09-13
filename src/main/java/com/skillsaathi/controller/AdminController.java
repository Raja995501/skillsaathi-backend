package com.skillsaathi.controller;

import com.skillsaathi.dto.admin.*;
import com.skillsaathi.dto.common.ApiResponse;
import com.skillsaathi.dto.report.ReportResponse;
import com.skillsaathi.dto.skill.SkillCategoryResponse;
import com.skillsaathi.dto.skill.SkillResponse;
import com.skillsaathi.entity.enums.ReportStatus;
import com.skillsaathi.exception.BadRequestException;
import com.skillsaathi.service.AdminService;
import com.skillsaathi.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Every endpoint here is already gated by SecurityConfig's
 * .requestMatchers("/api/v1/admin/**").hasRole("ADMIN") rule — no per-method @PreAuthorize needed.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getDashboard()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<AdminUserSummaryResponse>>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AdminUserSummaryResponse> response = adminService.listUsers(search, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/users/{id}/block")
    public ResponseEntity<ApiResponse<Void>> blockUser(@PathVariable Long id) {
        adminService.setUserBlocked(id, true);
        return ResponseEntity.ok(ApiResponse.success("User blocked", null));
    }

    @PutMapping("/users/{id}/unblock")
    public ResponseEntity<ApiResponse<Void>> unblockUser(@PathVariable Long id) {
        adminService.setUserBlocked(id, false);
        return ResponseEntity.ok(ApiResponse.success("User unblocked", null));
    }

    @PostMapping("/skills/categories")
    public ResponseEntity<ApiResponse<SkillCategoryResponse>> createCategory(
            @Valid @RequestBody UpsertSkillCategoryRequest request) {
        SkillCategoryResponse response = adminService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Category created", response));
    }

    @DeleteMapping("/skills/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }

    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<SkillResponse>> createSkill(@Valid @RequestBody UpsertSkillRequest request) {
        SkillResponse response = adminService.createSkill(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Skill created", response));
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(@PathVariable Long id) {
        adminService.deleteSkill(id);
        return ResponseEntity.ok(ApiResponse.success("Skill deleted", null));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> listReports(
            @RequestParam(required = false) String status) {
        ReportStatus parsed = status != null ? parseStatus(status) : null;
        return ResponseEntity.ok(ApiResponse.success(reportService.listReports(parsed)));
    }

    @PutMapping("/reports/{id}")
    public ResponseEntity<ApiResponse<ReportResponse>> resolveReport(
            @PathVariable Long id, @Valid @RequestBody ResolveReportRequest request) {
        ReportStatus newStatus = parseStatus(request.getStatus());
        ReportResponse response = reportService.resolveReport(id, newStatus);
        return ResponseEntity.ok(ApiResponse.success("Report updated", response));
    }

    private ReportStatus parseStatus(String status) {
        try {
            return ReportStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid status value: " + status);
        }
    }
}
