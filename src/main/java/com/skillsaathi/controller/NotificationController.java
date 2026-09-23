package com.skillsaathi.controller;

import com.skillsaathi.dto.common.ApiResponse;
import com.skillsaathi.dto.notification.NotificationResponse;
import com.skillsaathi.entity.PushSubscription;
import com.skillsaathi.repository.PushSubscriptionRepository;
import com.skillsaathi.service.NotificationService;
import com.skillsaathi.util.SecurityUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final PushSubscriptionRepository pushSubscriptionRepository;

    @Value("${app.vapid.public.key}")
    private String vapidPublicKey;

    // ===== EXISTING METHODS =====

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<NotificationResponse> response = notificationService.getNotifications(
                SecurityUtils.getCurrentUserId(), read, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> unreadCount() {
        long count = notificationService.unreadCount(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(SecurityUtils.getCurrentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    // ===== NEW: WEB PUSH METHODS =====

    /**
     * Frontend calls this to get VAPID public key for pushManager.subscribe().
     * Public endpoint — no auth needed.
     */
    @GetMapping("/push/public-key")
    public ResponseEntity<ApiResponse<Map<String, String>>> getPushPublicKey() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("publicKey", vapidPublicKey)));
    }

    /**
     * Frontend calls this after pushManager.subscribe() succeeds.
     * Saves endpoint + keys for the logged-in user.
     */
    @PostMapping("/push/subscribe")
    public ResponseEntity<ApiResponse<Void>> subscribeToPush(
            @RequestBody PushSubscribeRequest request) {

        Long userId = SecurityUtils.getCurrentUserId();

        // Avoid duplicate — same user + same endpoint = update keys
        pushSubscriptionRepository.findByUserId(userId).stream()
                .filter(s -> s.getEndpoint().equals(request.getEndpoint()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> {
                            existing.setP256dhKey(request.getP256dhKey());
                            existing.setAuthKey(request.getAuthKey());
                            pushSubscriptionRepository.save(existing);
                        },
                        () -> {
                            PushSubscription sub = new PushSubscription();
                            sub.setUserId(userId);
                            sub.setEndpoint(request.getEndpoint());
                            sub.setP256dhKey(request.getP256dhKey());
                            sub.setAuthKey(request.getAuthKey());
                            pushSubscriptionRepository.save(sub);
                        }
                );

        return ResponseEntity.ok(ApiResponse.success("Subscribed", null));
    }

    /**
     * Optional: frontend calls this on logout to remove subscription.
     */
    @DeleteMapping("/push/unsubscribe")
    public ResponseEntity<ApiResponse<Void>> unsubscribeFromPush(
            @RequestParam String endpoint) {

        Long userId = SecurityUtils.getCurrentUserId();

        pushSubscriptionRepository.findByUserId(userId).stream()
                .filter(s -> s.getEndpoint().equals(endpoint))
                .findFirst()
                .ifPresent(pushSubscriptionRepository::delete);

        return ResponseEntity.ok(ApiResponse.success("Unsubscribed", null));
    }

    @Data
    public static class PushSubscribeRequest {
        private String endpoint;
        private String p256dhKey;
        private String authKey;
    }
}