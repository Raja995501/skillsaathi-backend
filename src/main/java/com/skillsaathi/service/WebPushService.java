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
            // New API: constructor-based init
            pushService = new PushService(publicKey, privateKey, subject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendNotificationToUser(Long userId, String payload) {
        List<PushSubscription> subscriptions = subscriptionRepository.findByUserId(userId);

        for (PushSubscription sub : subscriptions) {
            try {
                Notification notification = Notification.builder()
                        .endpoint(sub.getEndpoint())
                        .userPublicKey(sub.getP256dhKey())
                        .userAuth(sub.getAuthKey())
                        .payload(payload.getBytes(StandardCharsets.UTF_8))
                        .build();

                pushService.send(notification);
            } catch (Exception e) {
                System.err.println("Failed to send push notification: " + e.getMessage());
            }
        }
    }
}