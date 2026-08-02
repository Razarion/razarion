package com.btxtech.server.repository;

import com.btxtech.server.model.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByUserId(String userId);

    // With the level joined in: the callers read it, and one lazy select per user was the whole
    // point of asking for them in a single query.
    @EntityGraph(attributePaths = "level")
    List<UserEntity> findByUserIdIn(Collection<String> userIds);

    /**
     * Every user with the two references the backend table reads, joined in. A plain findAll() left
     * them as lazy proxies and cost two more selects per row.
     * <p>
     * The collections are not in the graph: fetching two of them at once is a cartesian product,
     * and Hibernate refuses it outright for List-valued ones. They are batched instead - see the
     * &#64;BatchSize on the entity.
     */
    @EntityGraph(attributePaths = {"level", "activeQuest"})
    @Query("SELECT u FROM UserEntity u")
    List<UserEntity> findAllForBackendInfo();

    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT u FROM UserEntity u WHERE u.systemConnectionClosed IS NOT NULL AND u.systemConnectionClosed < :cutoff")
    List<UserEntity> findInactiveSince(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT u FROM UserEntity u WHERE u.verificationStartedDate IS NOT NULL AND u.verificationDoneDate IS NULL AND u.verificationStartedDate < :cutoff")
    List<UserEntity> findUnverifiedUsersOlderThan(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT u FROM UserEntity u WHERE u.verificationId = :verificationId " +
            "AND u.verificationDoneDate IS NULL ")
    List<UserEntity> findPendingVerificationUsers(@Param("verificationId") String verificationId);

}
