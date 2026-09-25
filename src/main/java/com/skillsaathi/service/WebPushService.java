package com.skillsaathi.service;

import com.skillsaathi.entity.PushSubscription;
import com.skillsaathi.repository.PushSubscriptionRepository;
import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.List;

@Service
public class WebPushService {

    private final PushSubscriptionRepository subscriptionRepository;

    @Value("${app.vapid.public.key}")
    private String publicKey;

    @Value("${app.vapid.private.key}")
    private String privateKey;

    @Value("${app.vapid.subject}")
    private String subject;

    private PushService pushService;

    public WebPushService(PushSubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @PostConstruct
    public void init() {
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }

            pushService = new PushService(publicKey, privateKey, subject);
            System.out.println("✅ WebPushService initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ WebPushService init FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendNotificationToUser(Long userId, String payload) {
        List<PushSubscription> subscriptions = subscriptionRepository.findByUserId(userId);

        System.out.println("🔔 PUSH ATTEMPT: userId=" + userId +
                ", subscriptions=" + subscriptions.size());

        if (subscriptions.isEmpty()) {
            System.out.println("⚠️ No push subscriptions for user " + userId);
            return;
        }

        for (PushSubscription sub : subscriptions) {
            try {
                Notification notification = Notification.builder()
                        .endpoint(sub.getEndpoint())
                        .userPublicKey(sub.getP256dhKey())
                        .userAuth(sub.getAuthKey())
                        .payload(payload.getBytes(StandardCharsets.UTF_8))
                        .build();

                pushService.send(notification);
                System.out.println("✅ Push SENT to user " + userId);
            } catch (Exception e) {
                System.err.println("❌ Push FAILED: " + e.getClass().getSimpleName()
                        + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}