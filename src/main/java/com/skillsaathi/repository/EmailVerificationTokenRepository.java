package com.skillsaathi.repository;

import com.skillsaathi.entity.EmailVerificationToken;
import com.skillsaathi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    void deleteByUserId(Long userId);

    // Agar User object pass karna chahte hain toh ye bhi rakh sakte hain:
    void deleteByUser(User user);
}
