package com.skillsaathi.websocket;

import com.skillsaathi.dto.chat.PresenceEventResponse;
import com.skillsaathi.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * Broadcasts online/offline transitions whenever a user's STOMP session connects or drops.
 * Published on a single shared "/topic/presence" destination — clients (e.g. anyone with that
 * user's chat open) filter client-side by userId, which is simpler than maintaining a
 * per-connection subscriber list server-side just for presence.
 */
@Component
@RequiredArgsConstructor
public class PresenceEventListener {

    private static final String PRESENCE_DESTINATION = "/topic/presence";

    private final ChatEventPublisher chatEventPublisher;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        extractUserId(event.getUser()).ifPresent(userId ->
                chatEventPublisher.publish(WsEventType.PRESENCE, PRESENCE_DESTINATION,
                        PresenceEventResponse.builder().userId(userId).online(true).build()));
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        extractUserId(accessor.getUser()).ifPresent(userId ->
                chatEventPublisher.publish(WsEventType.PRESENCE, PRESENCE_DESTINATION,
                        PresenceEventResponse.builder().userId(userId).online(false).build()));
    }

    private java.util.Optional<Long> extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return java.util.Optional.of(userDetails.getId());
        }
        return java.util.Optional.empty();
    }
}
