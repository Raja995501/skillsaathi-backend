package com.skillsaathi.entity;

import com.skillsaathi.entity.enums.SkillLevel;
import com.skillsaathi.entity.enums.SkillType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_skill", indexes = {
        @Index(name = "idx_userskill_skill_type", columnList = "skill_id, type")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_skill_type", columnNames = {"user_id", "skill_id", "type"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SkillType type; // TEACH, LEARN

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SkillLevel level;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
