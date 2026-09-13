package com.skillsaathi.repository;

import com.skillsaathi.entity.Connection;
import com.skillsaathi.entity.enums.ConnectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    Optional<Connection> findByRequesterIdAndReceiverId(Long requesterId, Long receiverId);

    List<Connection> findByReceiverIdAndStatus(Long receiverId, ConnectionStatus status);

    List<Connection> findByRequesterIdAndStatus(Long requesterId, ConnectionStatus status);

    @Query("""
        SELECT c FROM Connection c
        WHERE (c.requester.id = :userId OR c.receiver.id = :userId)
          AND c.status = :status
        """)
    List<Connection> findAllForUserByStatus(@Param("userId") Long userId,
                                             @Param("status") ConnectionStatus status);
}
