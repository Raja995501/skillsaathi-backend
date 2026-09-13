package com.skillsaathi.service;

import com.skillsaathi.dto.admin.*;
import com.skillsaathi.dto.skill.SkillCategoryResponse;
import com.skillsaathi.dto.skill.SkillResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    AdminDashboardResponse getDashboard();

    Page<AdminUserSummaryResponse> listUsers(String search, Pageable pageable);
    void setUserBlocked(Long userId, boolean blocked);

    SkillCategoryResponse createCategory(UpsertSkillCategoryRequest request);
    void deleteCategory(Long categoryId);

    SkillResponse createSkill(UpsertSkillRequest request);
    void deleteSkill(Long skillId);
}
