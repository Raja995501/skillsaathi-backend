package com.skillsaathi.entity;

import com.skillsaathi.entity.enums.AvailabilityType;
import com.skillsaathi.entity.enums.Gender;
import com.skillsaathi.entity.enums.OnlinePreference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user", indexes = {
        @Index(name = "idx_user_city", columnList = "city"),
        @Index(name = "idx_user_state", columnList = "state"),
        @Index(name = "idx_user_email", columnList = "email")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password; // BCrypt hash

    @Column(length = 15)
    private String phone;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 255)
    private String languages; // comma separated

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private AvailabilityType availability;

    @Enumerated(EnumType.STRING)
    @Column(name = "online_preference", length = 20)
    private OnlinePreference onlinePreference;

    @Column(name = "is_email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "is_blocked", nullable = false)
    @Builder.Default
    private boolean blocked = false;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
