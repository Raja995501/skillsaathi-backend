package com.skillsaathi.config;

import com.skillsaathi.entity.Role;
import com.skillsaathi.entity.SkillCategory;
import com.skillsaathi.repository.RoleRepository;
import com.skillsaathi.repository.SkillCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds mandatory lookup data (roles, skill categories) on startup so a fresh
 * environment is immediately usable without a manual SQL step.
 * Category list mirrors the SkillSaathi prototype's "Explore Skills" section exactly.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final SkillCategoryRepository skillCategoryRepository;

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Education", "Technology", "Design", "Music", "Languages",
            "Photography", "Business", "Sports", "Home Skills", "Local Skills"
    );

    @Override
    public void run(String... args) {
        List.of("USER", "ADMIN").forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
            }
        });

        if (skillCategoryRepository.count() == 0) {
            DEFAULT_CATEGORIES.forEach(name ->
                    skillCategoryRepository.save(SkillCategory.builder().name(name).build()));
        }
    }
}
