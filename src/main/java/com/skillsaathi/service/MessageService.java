package com.skillsaathi.service;

import com.skillsaathi.dto.chat.MessageResponse;
import com.skillsaathi.entity.enums.MessageType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MessageService {
    MessageResponse sendMessage(Long senderId, Long connectionId, String content, String fileUrl, MessageType type, Long replyToId);

    Page<MessageResponse> getHistory(Long userId, Long connectionId, Pageable pageable);

    int markAsRead(Long userId, Long connectionId);

    void deleteForMe(Long userId, Long messageId);

    void deleteForEveryone(Long userId, Long messageId);

    // ✅ EDIT FEATURE
    MessageResponse editMessage(Long userId, Long messageId, String newContent);
}