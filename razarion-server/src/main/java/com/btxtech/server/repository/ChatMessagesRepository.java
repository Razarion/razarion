package com.btxtech.server.repository;

import com.btxtech.server.model.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessagesRepository extends JpaRepository<ChatMessageEntity, Long> {
    /** The latest ones, newest first: what a client joining the chat is shown. */
    List<ChatMessageEntity> findTop50ByOrderByIdDesc();

    /** For the recorder, oldest first, so it can go on from the last one it has seen. */
    List<ChatMessageEntity> findTop200ByIdGreaterThanOrderByIdAsc(Long id);

    /** Where the recorder starts when it has seen nothing yet: from now on, not from the archive. */
    Optional<ChatMessageEntity> findTopByOrderByIdDesc();

    /** For the backend, newest first: what was written in a span. */
    List<ChatMessageEntity> findTop1000ByTimestampBetweenOrderByIdDesc(Date from, Date to);

    /** Whether a reel was announced already: the social pipeline may report one twice. */
    boolean existsByInstagramUrl(String instagramUrl);
}
