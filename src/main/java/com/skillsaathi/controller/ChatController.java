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

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long connectionId,
            @PathVariable Long messageId,
            @RequestParam(defaultValue = "false") boolean forEveryone) {

        Long userId = SecurityUtils.getCurrentUserId();

        if (forEveryone) {
            messageService.deleteForEveryone(userId, messageId);
        } else {
            messageService.deleteForMe(userId, messageId);
        }

        String destination = "/topic/connection." + connectionId + ".delete";
        chatEventPublisher.publish(WsEventType.NEW_MESSAGE, destination, Map.of(
                "messageId", messageId,
                "deletedByUserId", userId,
                "forEveryone", forEveryone
        ));

        return ResponseEntity.ok(ApiResponse.success("Deleted", null));
    }

    // ✅ EDIT endpoint
    @PatchMapping("/{messageId}")
    public ResponseEntity<ApiResponse<MessageResponse>> editMessage(
            @PathVariable Long connectionId,
            @PathVariable Long messageId,
            @RequestBody Map<String, String> payload) {

        Long userId = SecurityUtils.getCurrentUserId();
        String newContent = payload.get("content");

        MessageResponse updated = messageService.editMessage(userId, messageId, newContent);

        // Broadcast
        String destination = "/topic/connection." + connectionId + ".edit";
        chatEventPublisher.publish(WsEventType.NEW_MESSAGE, destination, updated);

        return ResponseEntity.ok(ApiResponse.success(updated));
    }
}