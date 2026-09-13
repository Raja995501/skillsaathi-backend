package com.skillsaathi.service.impl;

import com.skillsaathi.dto.chat.MessageResponse;
import com.skillsaathi.entity.Connection;
import com.skillsaathi.entity.Message;
import com.skillsaathi.entity.User;
import com.skillsaathi.entity.enums.ConnectionStatus;
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

    @Override
    public MessageResponse sendMessage(Long senderId, Long connectionId, String content) {
        Connection connection = getUnlockedConnection(connectionId, senderId);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        User receiver = connection.getRequester().getId().equals(senderId)
                ? connection.getReceiver() : connection.getRequester();

        Message message = Message.builder()
                .connection(connection)
                .sender(sender)
                .receiver(receiver)
                .content(content)
                .build(); // status defaults to SENT

        messageRepository.save(message);
        notificationService.create(receiver.getId(), NotificationType.NEW_MESSAGE,
                "New message from " + sender.getName(),
                content.length() > 80 ? content.substring(0, 80) + "..." : content,
                message.getId());

        return toResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getHistory(Long userId, Long connectionId, Pageable pageable) {
        Connection connection = getOwnedConnection(connectionId, userId);
        // History is viewable for any connection the user is part of, even if not yet accepted,
        // so a receiver can see the request context before deciding — but sending is gated
        // in sendMessage() via getUnlockedConnection().
        return messageRepository.findByConnectionIdOrderByCreatedAtDesc(connection.getId(), pageable)
                .map(this::toResponse);
    }

    @Override
    public int markAsRead(Long userId, Long connectionId) {
        getOwnedConnection(connectionId, userId);
        return messageRepository.markAllAsRead(connectionId, userId);
    }

    /** Chat only unlocks once the connection is ACCEPTED — this is the enforcement point. */
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
                .createdAt(m.getCreatedAt())
                .build();
    }
}
