package com.skillsaathi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "skill_category")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkillCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name; // Education, Technology, Design, ...
}
