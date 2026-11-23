package com.btxtech.server.repository;

import com.btxtech.server.model.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
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

    /** The backend's default user view: the newest players. Same graph as findAllForBackendInfo. */
    @EntityGraph(attributePaths = {"level", "activeQuest"})
    @Query("SELECT u FROM UserEntity u ORDER BY u.creationDate DESC")
    List<UserEntity> findNewestForBackendInfo(Pageable pageable);

    /**
     * The newest registrations. Registering sets the email and starts the verification; a user from
     * before verification dates existed falls back to when it was created.
     */
    @EntityGraph(attributePaths = {"level", "activeQuest"})
    @Query("SELECT u FROM UserEntity u WHERE u.email IS NOT NULL "
            + "ORDER BY COALESCE(u.verificationStartedDate, u.creationDate) DESC")
    List<UserEntity> findNewestRegisteredForBackendInfo(Pageable pageable);

    /**
     * The newest players who saved their progress by the mail link (or Facebook). They keep no
     * email, so the registrations above never show them - since the password registration left the
     * game on 2026-11-17, this is where new accounts appear.
     */
    @EntityGraph(attributePaths = {"level", "activeQuest"})
    @Query("SELECT u FROM UserEntity u WHERE u.progressSavedDate IS NOT NULL ORDER BY u.progressSavedDate DESC")
    List<UserEntity> findNewestSavedForBackendInfo(Pageable pageable);

    /**
     * The backend user search: a part of the name, the email or the user id, or one of the given
     * user ids - those whose call sign matched, which the database cannot compute.
     */
    @EntityGraph(attributePaths = {"level", "activeQuest"})
    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.name) LIKE :pattern OR LOWER(u.email) LIKE :pattern "
            + "OR LOWER(u.userId) LIKE :pattern OR u.userId IN :userIds ORDER BY u.creationDate DESC")
    List<UserEntity> searchForBackendInfo(@Param("pattern") String pattern,
                                          @Param("userIds") Collection<String> userIds,
                                          Pageable pageable);

    @Query("SELECT u.userId FROM UserEntity u WHERE u.userId IS NOT NULL")
    List<String> findAllUserIds();

    boolean existsByNameIgnoreCase(String name);

    Optional<UserEntity> findByAnonymousToken(String anonymousToken);

    /**
     * Disconnected players who have nothing left to come back to: no cookie that could find them
     * again, gone past the grace; or a cookie, gone past the retention. Unregistered only - the
     * caller checks it once more, a wrong delete here costs an account. A player who saved his
     * progress is never in here, however long he stays away.
     */
    @Query("SELECT u FROM UserEntity u WHERE u.systemConnectionClosed IS NOT NULL "
            + "AND u.verificationStartedDate IS NULL AND u.verificationDoneDate IS NULL "
            + "AND u.progressSavedDate IS NULL "
            + "AND ((u.anonymousToken IS NULL AND u.systemConnectionClosed < :graceCutoff) "
            + "OR u.systemConnectionClosed < :retentionCutoff)")
    List<UserEntity> findInactiveForGood(@Param("graceCutoff") LocalDateTime graceCutoff,
                                         @Param("retentionCutoff") LocalDateTime retentionCutoff);

    /**
     * Unregistered players who can still come back, gone past the grace: their base has to leave
     * the start region, their progress stays. Only the ids - this is every such player of the last
     * two weeks, once a minute. A player who saved his progress keeps his base, like a registered
     * one: he said he is coming back.
     */
    @Query("SELECT u.userId FROM UserEntity u WHERE u.anonymousToken IS NOT NULL "
            + "AND u.verificationStartedDate IS NULL AND u.verificationDoneDate IS NULL "
            + "AND u.progressSavedDate IS NULL "
            + "AND u.systemConnectionClosed IS NOT NULL AND u.systemConnectionClosed < :graceCutoff")
    List<String> findRetainedUserIdsInactiveSince(@Param("graceCutoff") LocalDateTime graceCutoff);

    /**
     * Users whose connection is open as far as the database knows, which is not the same as open.
     * <p>
     * The close timestamp is written by the websocket close event alone, and no idle timeout is
     * configured, so a socket that dies without a close frame - a locked phone, a swiped-away tab,
     * a network that simply stops - never produces one. The row then says "still connected"
     * forever, and {@link #findInactiveForGood} can never see it because that asks for a close time.
     * Measured on PROD on 2026-09-16: one of the four anonymous players who had actually connected
     * was in this state, and the row survives until the next server start.
     */
    @Query("SELECT u FROM UserEntity u WHERE u.systemConnectionOpened IS NOT NULL "
            + "AND u.systemConnectionClosed IS NULL AND u.systemConnectionOpened < :cutoff")
    List<UserEntity> findOpenSince(@Param("cutoff") LocalDateTime cutoff);

    /**
     * Users that never opened a connection at all.
     * <p>
     * Every game start writes more than one of these: the tracking beacon creates an anonymous user
     * before anybody plays, and only one of them goes on to connect. They own nothing - no base, no
     * spawn point - but they are invisible to every cleanup there is, because all of those key on a
     * connection timestamp and these have none. Eleven of the fifteen anonymous rows on PROD on
     * 2026-09-16 were this, about nine an hour, cleared only by a server restart.
     */
    @Query("SELECT u FROM UserEntity u WHERE u.systemConnectionOpened IS NULL AND u.creationDate < :cutoff")
    List<UserEntity> findNeverConnectedBefore(@Param("cutoff") Date cutoff);

    @Query("SELECT u FROM UserEntity u WHERE u.verificationStartedDate IS NOT NULL AND u.verificationDoneDate IS NULL AND u.verificationStartedDate < :cutoff")
    List<UserEntity> findUnverifiedUsersOlderThan(@Param("cutoff") LocalDateTime cutoff);

    @Query("SELECT u FROM UserEntity u WHERE u.verificationId = :verificationId " +
            "AND u.verificationDoneDate IS NULL ")
    List<UserEntity> findPendingVerificationUsers(@Param("verificationId") String verificationId);

    /**
     * Turns an anonymous player into a registered one, writing only the columns registration owns.
     * Deliberately not a save() of the loaded entity: registration reads the user, sends a mail and
     * only then writes, while the game-engine thread persists level and xp for the same row - see
     * {@link com.btxtech.server.user.UserService#registerByEmail}.
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE UserEntity u
               SET u.email = :email,
                   u.passwordHash = :passwordHash,
                   u.verificationId = :verificationId,
                   u.verificationStartedDate = :verificationStartedDate,
                   u.verificationDoneDate = NULL
             WHERE u.userId = :userId
            """)
    int applyRegistration(@Param("userId") String userId,
                          @Param("email") String email,
                          @Param("passwordHash") String passwordHash,
                          @Param("verificationId") String verificationId,
                          @Param("verificationStartedDate") Date verificationStartedDate);

    Optional<UserEntity> findByEmailHash(String emailHash);

    Optional<UserEntity> findByLoginLinkHash(String loginLinkHash);

    Optional<UserEntity> findByFacebookIdHash(String facebookIdHash);

    /** "Continue with Facebook" saved this player: the fingerprint of his app-scoped Facebook id. */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
            UPDATE UserEntity u
               SET u.facebookIdHash = :facebookIdHash,
                   u.progressSavedDate = :progressSavedDate
             WHERE u.userId = :userId
            """)
    int applyFacebookLink(@Param("userId") String userId,
                          @Param("facebookIdHash") String facebookIdHash,
                          @Param("progressSavedDate") Date progressSavedDate);

    /** Facebook's data deletion request: the link goes, the player and his progress stay. */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE UserEntity u SET u.facebookIdHash = NULL WHERE u.facebookIdHash = :facebookIdHash")
    int removeFacebookLink(@Param("facebookIdHash") String facebookIdHash);

    /**
     * Opens a save or sign-in link for this player: the hash of the link, how long it holds, and for a
     * save the fingerprint it will store. Targeted for the same reason as {@link #applyRegistration}:
     * the game engine writes level and xp to the same row meanwhile.
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE UserEntity u
               SET u.loginLinkHash = :loginLinkHash,
                   u.loginLinkExpiry = :loginLinkExpiry,
                   u.pendingEmailHash = :pendingEmailHash
             WHERE u.userId = :userId
            """)
    int applyLoginLink(@Param("userId") String userId,
                       @Param("loginLinkHash") String loginLinkHash,
                       @Param("loginLinkExpiry") Date loginLinkExpiry,
                       @Param("pendingEmailHash") String pendingEmailHash);

    /**
     * The link is used up; for a save link the fingerprint moves over to stay. Clears the persistence
     * context: the request that opens the link loaded this row before, and the cookie written right
     * after must see the fingerprint (open-in-view keeps one context per request).
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
            UPDATE UserEntity u
               SET u.emailHash = :emailHash,
                   u.progressSavedDate = :progressSavedDate,
                   u.pendingEmailHash = NULL,
                   u.loginLinkHash = NULL,
                   u.loginLinkExpiry = NULL
             WHERE u.userId = :userId
            """)
    int applyLinkUsed(@Param("userId") String userId,
                      @Param("emailHash") String emailHash,
                      @Param("progressSavedDate") Date progressSavedDate);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE UserEntity u SET u.anonymousToken = :anonymousToken WHERE u.userId = :userId AND u.anonymousToken IS NULL")
    int applyAnonymousTokenIfMissing(@Param("userId") String userId, @Param("anonymousToken") String anonymousToken);

}
