package com.skillsaathi.service;

import com.skillsaathi.dto.chat.MessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    /** Persists the message and returns it. Publishing to WS clients is handled by the caller (STOMP controller). */
    MessageResponse sendMessage(Long senderId, Long connectionId, String content);

    Page<MessageResponse> getHistory(Long userId, Long connectionId, Pageable pageable);

    int markAsRead(Long userId, Long connectionId);
}
