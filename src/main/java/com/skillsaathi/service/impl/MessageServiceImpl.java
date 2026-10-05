package com.skillsaathi.service.impl;

import com.skillsaathi.dto.chat.MessageResponse;
import com.skillsaathi.entity.Connection;
import com.skillsaathi.entity.Message;
import com.skillsaathi.entity.User;
import com.skillsaathi.entity.enums.ConnectionStatus;
import com.skillsaathi.entity.enums.MessageType; // === ADDED FOR MEDIA ===
import com.skillsaathi.entity.enums.NotificationType;
import com.skillsaathi.exception.BadRequestException;
import com.skillsaathi.exception.ResourceNotFoundException;
import com.skillsaathi.repository.ConnectionRepository;
import com.skillsaathi.repository.MessageRepository;
import com.skillsaathi.repository.UserRepository;
import com.skillsaathi.service.MessageService;
import com.skillsaathi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // === UPDATED FOR MEDIA ===
    @Override
    public MessageResponse sendMessage(Long senderId, Long connectionId, String content, String fileUrl, MessageType type) {
        Connection connection = getUnlockedConnection(connectionId, senderId);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = connection.getRequester().getId().equals(senderId)
                ? connection.getReceiver() : connection.getRequester();

        // Agar type null ho toh default TEXT set kar dein
        MessageType messageType = (type != null) ? type : MessageType.TEXT;

        Message message = Message.builder()
                .connection(connection)
                .sender(sender)
                .receiver(receiver)
                .content(content != null ? content : (messageType == MessageType.IMAGE ? "Shared an image" : "Shared a video"))
                .fileUrl(fileUrl)     // === ADDED FOR MEDIA ===
                .type(messageType)    // === ADDED FOR MEDIA ===
                .build();

        messageRepository.save(message);

        String notifText = messageType == MessageType.TEXT ? (content.length() > 80 ? content.substring(0, 80) + "..." : content) : "Sent a " + messageType.name().toLowerCase();

        notificationService.create(receiver.getId(), NotificationType.NEW_MESSAGE,
                "New message from " + sender.getName(),
                notifText,
                message.getId());

        return toResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getHistory(Long userId, Long connectionId, Pageable pageable) {
        Connection connection = getOwnedConnection(connectionId, userId);
        return messageRepository.findByConnectionIdOrderByCreatedAtDesc(connection.getId(), pageable)
                .map(this::toResponse);
    }

    @Override
    public int markAsRead(Long userId, Long connectionId) {
        getOwnedConnection(connectionId, userId);
        return messageRepository.markAllAsRead(connectionId, userId);
    }

    private Connection getUnlockedConnection(Long connectionId, Long userId) {
        Connection connection = getOwnedConnection(connectionId, userId);
        if (connection.getStatus() != ConnectionStatus.ACCEPTED) {
            throw new BadRequestException("Chat is only available once the connection request is accepted");
        }
        return connection;
    }

    private Connection getOwnedConnection(Long connectionId, Long userId) {
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found"));

        boolean involvesUser = connection.getRequester().getId().equals(userId)
                || connection.getReceiver().getId().equals(userId);
        if (!involvesUser) {
            throw new BadRequestException("You don't have access to this conversation");
        }
        return connection;
    }

    private MessageResponse toResponse(Message m) {
        return MessageResponse.builder()
                .id(m.getId())
                .connectionId(m.getConnection().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getName())
                .receiverId(m.getReceiver().getId())
                .content(m.getContent())
                .status(m.getStatus().name())
                .type(m.getType() != null ? m.getType().name() : "TEXT") // === ADDED FOR MEDIA ===
                .fileUrl(m.getFileUrl())                                 // === ADDED FOR MEDIA ===
                .createdAt(m.getCreatedAt())
                .build();
    }
}