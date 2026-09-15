package com.skillsaathi.controller;

import com.skillsaathi.dto.chat.ChatMessageRequest;
import com.skillsaathi.dto.chat.MessageResponse;
import com.skillsaathi.dto.chat.PresenceEventResponse;
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

        String destination = "/topic/connection." + request.getConnectionId() + ".typing";
        chatEventPublisher.publish(WsEventType.TYPING, destination, event);
    }

    // ✅ FIX: Map ki jagah PresenceEventResponse use kiya, taaki online field sahi se jaaye
    @MessageMapping("/user.presence")
    public void updatePresence(@Payload PresenceEventResponse request, Principal principal) {
        Long userId = extractUserId(principal);

        PresenceEventResponse event = PresenceEventResponse.builder()
                .userId(userId)
                .online(request.isOnline())
                .build();

        String destination = "/topic/presence";
        chatEventPublisher.publish(WsEventType.PRESENCE, destination, event);
    }

    private Long extractUserId(Principal principal) {
        if (principal == null) throw new IllegalStateException("WebSocket principal is null");

        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        throw new IllegalStateException("WebSocket session is not authenticated properly: " + principal.getClass().getName());
    }

    private String extractUserName(Principal principal) {
        if (principal == null) return "User";

        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            if (userDetails.getUser() != null) return userDetails.getUser().getName();
        }
        if (principal instanceof CustomUserDetails userDetails) {
            if (userDetails.getUser() != null) return userDetails.getUser().getName();
        }
        return "User";
    }
}
