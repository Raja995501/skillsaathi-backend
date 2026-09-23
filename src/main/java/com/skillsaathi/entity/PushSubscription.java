package com.skillsaathi.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "push_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // Kis user ka subscription hai

    @Column(columnDefinition = "TEXT")
    private String endpoint;

    @Column(columnDefinition = "TEXT")
    private String p256dhKey;

    @Column(columnDefinition = "TEXT")
    private String authKey;
}
