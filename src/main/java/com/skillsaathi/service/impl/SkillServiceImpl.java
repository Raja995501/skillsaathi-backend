package com.skillsaathi.service.impl;

import com.skillsaathi.dto.skill.*;
import com.skillsaathi.entity.Skill;
import com.skillsaathi.entity.SkillCategory;
import com.skillsaathi.entity.User;
import com.skillsaathi.entity.UserSkill;
import com.skillsaathi.entity.enums.SkillLevel;
import com.skillsaathi.entity.enums.SkillType;
import com.skillsaathi.exception.BadRequestException;
import com.skillsaathi.exception.ResourceNotFoundException;
import com.skillsaathi.repository.SkillCategoryRepository;
import com.skillsaathi.repository.SkillRepository;
import com.skillsaathi.repository.UserRepository;
import com.skillsaathi.repository.UserSkillRepository;
import com.skillsaathi.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SkillServiceImpl implements SkillService {

    private final SkillCategoryRepository skillCategoryRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SkillCategoryResponse> getAllCategories() {
        return skillCategoryRepository.findAll().stream()
                .map(c -> SkillCategoryResponse.builder().id(c.getId()).name(c.getName()).build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SkillResponse> getSkills(Long categoryId, String search) {
        List<Skill> skills;
        if (categoryId != null) {
            skills = skillRepository.findByCategoryId(categoryId);
        } else if (StringUtils.hasText(search)) {
            skills = skillRepository.findByNameContainingIgnoreCase(search);
        } else {
            skills = skillRepository.findAll();
        }

        return skills.stream()
                .map(s -> SkillResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .categoryId(s.getCategory().getId())
                        .categoryName(s.getCategory().getName())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSkillResponse> getMySkills(Long userId) {
        return userSkillRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserSkillResponse addUserSkill(Long userId, AddUserSkillRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        SkillType type = parseEnum(SkillType.class, request.getType(), "type");

        if (userSkillRepository.existsByUserIdAndSkillIdAndType(userId, skill.getId(), type)) {
            throw new BadRequestException("You've already added this skill as " + type);
        }

        SkillLevel level = request.getLevel() != null
                ? parseEnum(SkillLevel.class, request.getLevel(), "level")
                : null;

        UserSkill userSkill = UserSkill.builder()
                .user(user)
                .skill(skill)
                .type(type)
                .level(level)
                .build();

        userSkillRepository.save(userSkill);
        return toResponse(userSkill);
    }

    @Override
    public UserSkillResponse updateUserSkill(Long userId, Long userSkillId, UpdateUserSkillRequest request) {
        UserSkill userSkill = userSkillRepository.findById(userSkillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill entry not found"));

        if (!userSkill.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only edit your own skills");
        }

        if (request.getLevel() != null) {
            userSkill.setLevel(parseEnum(SkillLevel.class, request.getLevel(), "level"));
        }

        userSkillRepository.save(userSkill);
        return toResponse(userSkill);
    }

    @Override
    public void removeUserSkill(Long userId, Long userSkillId) {
        UserSkill userSkill = userSkillRepository.findById(userSkillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill entry not found"));

        if (!userSkill.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only remove your own skills");
        }

        userSkillRepository.delete(userSkill);
    }

    private UserSkillResponse toResponse(UserSkill s) {
        return UserSkillResponse.builder()
                .id(s.getId())
                .skillId(s.getSkill().getId())
                .skillName(s.getSkill().getName())
                .categoryName(s.getSkill().getCategory().getName())
                .type(s.getType().name())
                .level(s.getLevel() != null ? s.getLevel().name() : null)
                .build();
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value, String fieldName) {
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid value for " + fieldName + ": " + value);
        }
    }
}
