package com.skillsaathi.repository;

import com.skillsaathi.entity.User;
import com.skillsaathi.entity.enums.OnlinePreference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Used by search/filter endpoint
    @Query("""
        SELECT u FROM User u
        WHERE (:city IS NULL OR LOWER(u.city) = LOWER(:city))
          AND (:state IS NULL OR LOWER(u.state) = LOWER(:state))
          AND u.blocked = false
        """)
    Page<User> searchUsers(@Param("city") String city,
                           @Param("state") String state,
                           Pageable pageable);

    // Broader search used by the Search module
    @Query("""
        SELECT DISTINCT u FROM User u
        LEFT JOIN UserSkill us ON us.user = u
        WHERE (:city IS NULL OR LOWER(u.city) = LOWER(:city))
          AND (:state IS NULL OR LOWER(u.state) = LOWER(:state))
          AND (:categoryId IS NULL OR us.skill.category.id = :categoryId)
          AND (:onlinePreference IS NULL OR u.onlinePreference = :onlinePreference
               OR u.onlinePreference = com.skillsaathi.entity.enums.OnlinePreference.BOTH)
          AND u.blocked = false
          AND u.id <> :excludeUserId
        """)
    List<User> searchUsersAdvanced(@Param("city") String city,
                                   @Param("state") String state,
                                   @Param("categoryId") Long categoryId,
                                   @Param("onlinePreference") OnlinePreference onlinePreference,
                                   @Param("excludeUserId") Long excludeUserId);

    // Admin user listing — simple keyword search across name/email
    @Query("""
        SELECT u FROM User u
        WHERE (:keyword IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}