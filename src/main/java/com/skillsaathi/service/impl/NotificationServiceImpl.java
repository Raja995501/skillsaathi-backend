package com.skillsaathi.service.impl;

import com.skillsaathi.dto.notification.NotificationResponse;
import com.skillsaathi.entity.Notification;
import com.skillsaathi.entity.User;
import com.skillsaathi.entity.enums.NotificationType;
import com.skillsaathi.exception.ResourceNotFoundException;
import com.skillsaathi.repository.NotificationRepository;
import com.skillsaathi.repository.UserRepository;
import com.skillsaathi.service.NotificationService;
import com.skillsaathi.websocket.ChatEventPublisher;
import com.skillsaathi.websocket.WsEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ChatEventPublisher chatEventPublisher;

    @Override
    public void create(Long userId, NotificationType type, String title, String body, Long referenceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .referenceId(referenceId)
                .read(false)
                .build();

        notificationRepository.save(notification);

        // Live push — if the user has an open WebSocket session it arrives instantly;
        // if not, it's already safely persisted and shows up next time they call GET /notifications.
        String destination = "/topic/user." + userId + ".notifications";
        chatEventPublisher.publish(WsEventType.NOTIFICATION, destination, toResponse(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, Boolean read, Pageable pageable) {
        Page<Notification> notifications = (read != null)
                ? notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, read, pageable)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return notifications.map(this::toResponse);
    }

    @Override
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found"); // don't leak existence to other users
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType().name())
                .title(n.getTitle())
                .body(n.getBody())
                .referenceId(n.getReferenceId())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
