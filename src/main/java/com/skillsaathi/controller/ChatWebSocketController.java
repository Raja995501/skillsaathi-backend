package com.skillsaathi.controller;

import com.skillsaathi.dto.chat.ChatMessageRequest;
import com.skillsaathi.dto.chat.MessageResponse;
import com.skillsaathi.dto.chat.TypingEventRequest;
import com.skillsaathi.dto.chat.TypingEventResponse;
import com.skillsaathi.security.CustomUserDetails;
import com.skillsaathi.service.MessageService;
import com.skillsaathi.websocket.ChatEventPublisher;
import com.skillsaathi.websocket.WsEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * STOMP message handlers. Every handler here does the SAME thing on success: persist/validate,
 * then publish to Redis (never call SimpMessagingTemplate directly) — see ChatEventPublisher/
 * ChatEventSubscriber for why: it's what makes delivery correct across multiple backend instances.
 */
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;
    private final ChatEventPublisher chatEventPublisher;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        Long senderId = extractUserId(principal);

        MessageResponse saved = messageService.sendMessage(senderId, request.getConnectionId(), request.getContent());

        String destination = "/topic/connection." + request.getConnectionId();
        chatEventPublisher.publish(WsEventType.NEW_MESSAGE, destination, saved);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingEventRequest request, Principal principal) {
        Long userId = extractUserId(principal);
        String userName = extractUserName(principal);

        TypingEventResponse event = TypingEventResponse.builder()
                .connectionId(request.getConnectionId())
                .userId(userId)
                .userName(userName)
                .typing(request.isTyping())
                .build();

        // Broadcast on the same per-connection topic, distinguished by destination suffix —
        // simpler than per-user /user/queue routing for a 1:1 chat where only two people
        // are ever subscribed to a given connection's topics anyway.
        String destination = "/topic/connection." + request.getConnectionId() + ".typing";
        chatEventPublisher.publish(WsEventType.TYPING, destination, event);
    }

    private Long extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new IllegalStateException("WebSocket session is not authenticated");
    }

    private String extractUserName(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUser().getName();
        }
        throw new IllegalStateException("WebSocket session is not authenticated");
    }
}
