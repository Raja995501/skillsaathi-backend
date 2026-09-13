package com.skillsaathi.repository;

import com.skillsaathi.entity.Message;
import com.skillsaathi.entity.enums.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConnectionIdOrderByCreatedAtDesc(Long connectionId, Pageable pageable);

    @Modifying
    @Query("""
        UPDATE Message m SET m.status = com.skillsaathi.entity.enums.MessageStatus.READ
        WHERE m.connection.id = :connectionId AND m.receiver.id = :receiverId
          AND m.status <> com.skillsaathi.entity.enums.MessageStatus.READ
        """)
    int markAllAsRead(@Param("connectionId") Long connectionId, @Param("receiverId") Long receiverId);
}
