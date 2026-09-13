package com.skillsaathi.controller;

import com.skillsaathi.dto.chat.MessageResponse;
import com.skillsaathi.dto.common.ApiResponse;
import com.skillsaathi.service.MessageService;
import com.skillsaathi.util.SecurityUtils;
import com.skillsaathi.websocket.ChatEventPublisher;
import com.skillsaathi.websocket.WsEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** REST endpoints for chat HISTORY. Live delivery happens over STOMP — see ChatWebSocketController. */
@RestController
@RequestMapping("/api/v1/connections/{connectionId}/messages")
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final ChatEventPublisher chatEventPublisher;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MessageResponse>>> getHistory(
            @PathVariable Long connectionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {

        Page<MessageResponse> history = messageService.getHistory(
                SecurityUtils.getCurrentUserId(), connectionId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @PostMapping("/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long connectionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        int updated = messageService.markAsRead(userId, connectionId);

        if (updated > 0) {
            String destination = "/topic/connection." + connectionId + ".read";
            chatEventPublisher.publish(WsEventType.READ_RECEIPT, destination,
                    Map.of("connectionId", connectionId, "readByUserId", userId));
        }

        return ResponseEntity.ok(ApiResponse.success("Marked as read", null));
    }
}
