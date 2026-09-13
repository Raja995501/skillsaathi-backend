package com.skillsaathi.service;

import com.skillsaathi.dto.notification.NotificationResponse;
import com.skillsaathi.entity.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /**
     * Persists a notification AND pushes it live over WebSocket to the target user
     * (if they're currently connected). Called from Connection/Message/Review services —
     * this is the single choke point other modules go through, so notification creation
     * and delivery never drift out of sync.
     */
    void create(Long userId, NotificationType type, String title, String body, Long referenceId);

    Page<NotificationResponse> getNotifications(Long userId, Boolean read, Pageable pageable);

    void markAsRead(Long userId, Long notificationId);

    void markAllAsRead(Long userId);

    long unreadCount(Long userId);
}
