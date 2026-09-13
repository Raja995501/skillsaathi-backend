package com.skillsaathi.repository;

import com.skillsaathi.entity.UserSkill;
import com.skillsaathi.entity.enums.SkillType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUserId(Long userId);

    List<UserSkill> findByUserIdAndType(Long userId, SkillType type);

    // Core to the matching engine: who teaches skill X / wants to learn skill X
    List<UserSkill> findBySkillIdAndType(Long skillId, SkillType type);

    // Batch version used to find all candidates matching any of "my" wanted/offered skills in one query
    List<UserSkill> findBySkillIdInAndType(List<Long> skillIds, SkillType type);

    boolean existsByUserIdAndSkillIdAndType(Long userId, Long skillId, SkillType type);

    List<UserSkill> findByUserIdIn(List<Long> userIds);
}
