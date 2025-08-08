package com.btxtech.server.repository;

import com.btxtech.server.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUserId(String userId);

    @Query("SELECT u FROM UserEntity u WHERE u.systemConnectionClosed IS NOT NULL AND u.systemConnectionClosed < :cutoff")
    List<UserEntity> findInactiveSince(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT u FROM UserEntity u WHERE u.verificationStartedDate IS NOT NULL AND u.verificationDoneDate IS NULL AND u.verificationStartedDate < :cutoff")
    List<UserEntity> findUnverifiedUsersOlderThan(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT u FROM UserEntity u WHERE u.verificationId = :verificationId " +
            "AND u.verificationDoneDate IS NULL ")
    List<UserEntity> findPendingVerificationUsers(@Param("verificationId") String verificationId);

}
