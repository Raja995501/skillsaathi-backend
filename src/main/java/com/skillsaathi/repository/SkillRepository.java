package com.skillsaathi.repository;

import com.skillsaathi.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByCategoryId(Long categoryId);
    List<Skill> findByNameContainingIgnoreCase(String name);
}
