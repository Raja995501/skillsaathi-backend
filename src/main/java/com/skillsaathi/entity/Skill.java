package com.skillsaathi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "skill", indexes = {
        @Index(name = "idx_skill_category", columnList = "category_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_skill_name_category", columnNames = {"name", "category_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private SkillCategory category;
}
