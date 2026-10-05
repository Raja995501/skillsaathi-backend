package com.skillsaathi.service;

import com.skillsaathi.dto.chat.MessageResponse;
import com.skillsaathi.entity.enums.MessageType; // === ADDED FOR MEDIA ===
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    /** Persists the message and returns it. Publishing to WS clients is handled by the caller (STOMP controller). */

    // === UPDATED FOR MEDIA ===
    MessageResponse sendMessage(Long senderId, Long connectionId, String content, String fileUrl, MessageType type);

    Page<MessageResponse> getHistory(Long userId, Long connectionId, Pageable pageable);

    int markAsRead(Long userId, Long connectionId);
}