package com.btxtech.server.repository;

import com.btxtech.server.model.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessagesRepository extends JpaRepository<ChatMessageEntity, Long> {
}
