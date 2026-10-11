package com.skillsaathi.entity;

import com.skillsaathi.entity.enums.MessageStatus;
import com.skillsaathi.entity.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "message", indexes = {
        @Index(name = "idx_message_connection_created", columnList = "connection_id, created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "connection_id", nullable = false)
    private Connection connection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    // === MEDIA (IMAGE/VIDEO) ===
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MessageType type = MessageType.TEXT;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    // === DELETE FEATURE ===
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "message_deleted_for_me",
            joinColumns = @JoinColumn(name = "message_id")
    )
    @Column(name = "user_id")
    @Builder.Default
    private List<Long> deletedForMe = new ArrayList<>();

    @Column(name = "deleted_for_everyone", nullable = false)
    @Builder.Default
    private Boolean deletedForEveryone = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    // ========================

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}