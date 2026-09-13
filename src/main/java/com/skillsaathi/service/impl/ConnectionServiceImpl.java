package com.skillsaathi.service.impl;

import com.skillsaathi.dto.connection.ConnectionResponse;
import com.skillsaathi.dto.match.MatchResponse;
import com.skillsaathi.entity.Connection;
import com.skillsaathi.entity.User;
import com.skillsaathi.entity.enums.ConnectionStatus;
import com.skillsaathi.entity.enums.NotificationType;
import com.skillsaathi.exception.BadRequestException;
import com.skillsaathi.exception.ResourceNotFoundException;
import com.skillsaathi.repository.ConnectionRepository;
import com.skillsaathi.repository.UserRepository;
import com.skillsaathi.service.ConnectionService;
import com.skillsaathi.service.MatchService;
import com.skillsaathi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ConnectionServiceImpl implements ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final MatchService matchService;
    private final NotificationService notificationService;

    @Override
    public ConnectionResponse sendRequest(Long requesterId, Long receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new BadRequestException("You can't send a connection request to yourself");
        }

        User requester = getUserOrThrow(requesterId);
        User receiver = getUserOrThrow(receiverId);

        if (receiver.isBlocked()) {
            throw new BadRequestException("This user is not available");
        }

        // A pending/accepted request already exists in EITHER direction -> reject duplicate.
        Optional<Connection> existingForward = connectionRepository.findByRequesterIdAndReceiverId(requesterId, receiverId);
        Optional<Connection> existingBackward = connectionRepository.findByRequesterIdAndReceiverId(receiverId, requesterId);

        if (existingForward.isPresent() && isActive(existingForward.get().getStatus())) {
            throw new BadRequestException("A connection request already exists with this user");
        }
        if (existingBackward.isPresent() && isActive(existingBackward.get().getStatus())) {
            throw new BadRequestException("This user has already sent you a connection request — check your incoming requests");
        }

        // Snapshot match info at request time (see Phase 0 design note: this freezes the reason
        // so it doesn't silently change later if either user edits their skills).
        MatchResponse matchInfo = matchService.getMatchDetail(requesterId, receiverId);

        Connection connection = Connection.builder()
                .requester(requester)
                .receiver(receiver)
                .status(ConnectionStatus.PENDING)
                .matchPercentage(matchInfo.getMatchPercentage())
                .matchReason(matchInfo.getMatchReason())
                .build();

        connectionRepository.save(connection);
        notificationService.create(receiverId, NotificationType.CONNECTION_REQUEST,
                "New connection request", requester.getName() + " wants to connect with you", connection.getId());

        return toResponse(connection, requesterId);
    }

    @Override
    public ConnectionResponse cancelRequest(Long userId, Long connectionId) {
        Connection connection = getOwnedConnection(connectionId, userId);

        if (!connection.getRequester().getId().equals(userId)) {
            throw new BadRequestException("Only the requester can cancel this request");
        }
        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new BadRequestException("Only pending requests can be cancelled");
        }

        connection.setStatus(ConnectionStatus.CANCELLED);
        connectionRepository.save(connection);
        return toResponse(connection, userId);
    }

    @Override
    public ConnectionResponse acceptRequest(Long userId, Long connectionId) {
        Connection connection = getOwnedConnection(connectionId, userId);

        if (!connection.getReceiver().getId().equals(userId)) {
            throw new BadRequestException("Only the receiver can accept this request");
        }
        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new BadRequestException("This request is no longer pending");
        }

        connection.setStatus(ConnectionStatus.ACCEPTED);
        connectionRepository.save(connection);
        notificationService.create(connection.getRequester().getId(), NotificationType.REQUEST_ACCEPTED,
                "Request accepted", connection.getReceiver().getName() + " accepted your connection request",
                connection.getId());

        return toResponse(connection, userId);
    }

    @Override
    public ConnectionResponse rejectRequest(Long userId, Long connectionId) {
        Connection connection = getOwnedConnection(connectionId, userId);

        if (!connection.getReceiver().getId().equals(userId)) {
            throw new BadRequestException("Only the receiver can reject this request");
        }
        if (connection.getStatus() != ConnectionStatus.PENDING) {
            throw new BadRequestException("This request is no longer pending");
        }

        connection.setStatus(ConnectionStatus.REJECTED);
        connectionRepository.save(connection);
        return toResponse(connection, userId);
    }

    @Override
    public ConnectionResponse blockConnection(Long userId, Long connectionId) {
        Connection connection = getOwnedConnection(connectionId, userId);
        connection.setStatus(ConnectionStatus.BLOCKED);
        connectionRepository.save(connection);
        return toResponse(connection, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConnectionResponse> listConnections(Long userId, String status) {
        List<Connection> connections;

        if (status != null && !status.isBlank()) {
            ConnectionStatus parsed = parseStatus(status);
            connections = connectionRepository.findAllForUserByStatus(userId, parsed);
        } else {
            // No status filter -> union of all statuses for this user
            connections = connectionRepository.findAllForUserByStatus(userId, ConnectionStatus.PENDING);
            connections.addAll(connectionRepository.findAllForUserByStatus(userId, ConnectionStatus.ACCEPTED));
        }

        return connections.stream().map(c -> toResponse(c, userId)).toList();
    }

    private Connection getOwnedConnection(Long connectionId, Long userId) {
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found"));

        boolean involvesUser = connection.getRequester().getId().equals(userId)
                || connection.getReceiver().getId().equals(userId);

        if (!involvesUser) {
            throw new BadRequestException("You don't have access to this connection");
        }
        return connection;
    }

    private boolean isActive(ConnectionStatus status) {
        return status == ConnectionStatus.PENDING || status == ConnectionStatus.ACCEPTED;
    }

    private ConnectionStatus parseStatus(String status) {
        try {
            return ConnectionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid status value: " + status);
        }
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ConnectionResponse toResponse(Connection c, Long viewerId) {
        boolean iAmRequester = c.getRequester().getId().equals(viewerId);
        User other = iAmRequester ? c.getReceiver() : c.getRequester();

        return ConnectionResponse.builder()
                .id(c.getId())
                .otherUserId(other.getId())
                .otherUserName(other.getName())
                .otherUserProfilePicture(other.getProfilePictureUrl())
                .direction(iAmRequester ? "OUTGOING" : "INCOMING")
                .status(c.getStatus().name())
                .matchPercentage(c.getMatchPercentage())
                .matchReason(c.getMatchReason())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
