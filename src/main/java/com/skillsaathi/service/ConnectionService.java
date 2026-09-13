package com.skillsaathi.service;

import com.skillsaathi.dto.connection.ConnectionResponse;

import java.util.List;

public interface ConnectionService {
    ConnectionResponse sendRequest(Long requesterId, Long receiverId);
    ConnectionResponse cancelRequest(Long userId, Long connectionId);
    ConnectionResponse acceptRequest(Long userId, Long connectionId);
    ConnectionResponse rejectRequest(Long userId, Long connectionId);
    ConnectionResponse blockConnection(Long userId, Long connectionId);
    List<ConnectionResponse> listConnections(Long userId, String status);
}
