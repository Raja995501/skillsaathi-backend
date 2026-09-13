package com.skillsaathi.service;

import com.skillsaathi.dto.skill.*;

import java.util.List;

public interface SkillService {
    List<SkillCategoryResponse> getAllCategories();
    List<SkillResponse> getSkills(Long categoryId, String search);
    List<UserSkillResponse> getMySkills(Long userId);
    UserSkillResponse addUserSkill(Long userId, AddUserSkillRequest request);
    UserSkillResponse updateUserSkill(Long userId, Long userSkillId, UpdateUserSkillRequest request);
    void removeUserSkill(Long userId, Long userSkillId);
}
