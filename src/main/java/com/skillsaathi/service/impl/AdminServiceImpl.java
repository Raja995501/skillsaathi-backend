package com.skillsaathi.service.impl;

import com.skillsaathi.dto.admin.*;
import com.skillsaathi.dto.skill.SkillCategoryResponse;
import com.skillsaathi.dto.skill.SkillResponse;
import com.skillsaathi.entity.Skill;
import com.skillsaathi.entity.SkillCategory;
import com.skillsaathi.entity.User;
import com.skillsaathi.exception.BadRequestException;
import com.skillsaathi.exception.ResourceNotFoundException;
import com.skillsaathi.repository.*;
import com.skillsaathi.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final ConnectionRepository connectionRepository;
    private final MessageRepository messageRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .totalSkills(skillRepository.count())
                .totalConnections(connectionRepository.count())
                .totalMessages(messageRepository.count())
                .totalReviews(reviewRepository.count())
                .openReports(reportRepository.findByStatus(com.skillsaathi.entity.enums.ReportStatus.OPEN).size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserSummaryResponse> listUsers(String search, Pageable pageable) {
        Page<User> users = userRepository.searchByKeyword(search, pageable);
        return users.map(u -> AdminUserSummaryResponse.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .city(u.getCity())
                .state(u.getState())
                .blocked(u.isBlocked())
                .emailVerified(u.isEmailVerified())
                .averageRating(u.getAverageRating())
                .createdAt(u.getCreatedAt())
                .build());
    }

    @Override
    public void setUserBlocked(Long userId, boolean blocked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setBlocked(blocked);
        userRepository.save(user);

        if (blocked) {
            // Kill active sessions immediately so a blocked user can't keep using an existing access
            // token's refresh cycle (the short-lived access token itself will still expire naturally
            // within app.jwt.access-token-expiry-ms, but this stops them getting a new one).
            refreshTokenRepository.deleteByUserId(userId);
        }
    }

    @Override
    public SkillCategoryResponse createCategory(UpsertSkillCategoryRequest request) {
        if (skillCategoryRepository.findAll().stream().anyMatch(c -> c.getName().equalsIgnoreCase(request.getName()))) {
            throw new BadRequestException("A category with this name already exists");
        }
        SkillCategory category = skillCategoryRepository.save(SkillCategory.builder().name(request.getName()).build());
        return SkillCategoryResponse.builder().id(category.getId()).name(category.getName()).build();
    }

    @Override
    public void deleteCategory(Long categoryId) {
        SkillCategory category = skillCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!skillRepository.findByCategoryId(categoryId).isEmpty()) {
            throw new BadRequestException("Can't delete a category that still has skills under it");
        }
        skillCategoryRepository.delete(category);
    }

    @Override
    public SkillResponse createSkill(UpsertSkillRequest request) {
        SkillCategory category = skillCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Skill skill = Skill.builder().name(request.getName()).category(category).build();
        skillRepository.save(skill);

        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .categoryId(category.getId())
                .categoryName(category.getName())
                .build();
    }

    @Override
    public void deleteSkill(Long skillId) {
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
        skillRepository.delete(skill); // cascades to user_skill rows via FK ON DELETE CASCADE
    }
}
