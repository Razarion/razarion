package com.btxtech.server.persistence.chat;

import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.shared.datatypes.UserContext;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Beat
 * on 29.12.2017.
 */
@Singleton
public class ChatPersistence {
    private static final int CACHE_SIZE = 20;
    @Inject
    private UserService userService;
    @Inject
    private ClientSystemConnectionService clientSystemConnectionService;
    @PersistenceContext
    private EntityManager entityManager;
    private final List<ChatMessage> chatMessages = new ArrayList<>();

    @Transactional
    public void fillCacheFromDb() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ChatMessageEntity> userQuery = criteriaBuilder.createQuery(ChatMessageEntity.class);
        Root<ChatMessageEntity> from = userQuery.from(ChatMessageEntity.class);
        CriteriaQuery<ChatMessageEntity> userSelect = userQuery.select(from);
        userQuery.orderBy(criteriaBuilder.desc(from.get(ChatMessageEntity_.timestamp)));
        List<ChatMessageEntity> chatMessageEntities = entityManager.createQuery(userSelect).setMaxResults(CACHE_SIZE).getResultList();
        synchronized (chatMessages) {
            chatMessages.clear();
            entityManager.createQuery(userSelect).setMaxResults(CACHE_SIZE).getResultList().forEach(chatMessageEntity -> {
                chatMessages.add(chatMessageEntity.toChatMessage());
            });
            Collections.reverse(chatMessages);
        }
    }

    @Transactional
    public void onMessage(PlayerSession playerSession, String message) {
        UserContext userContext = playerSession.getUserContext();
        if (!userContext.checkRegistered()) {
            throw new IllegalStateException("User is not registered. Session id:" + playerSession.getHttpSessionId());
        }
        if (!userContext.checkName()) {
            throw new IllegalStateException("User has no name: " + userContext);
        }
        ChatMessage chatMessage = new ChatMessage().setUserId(userContext.getHumanPlayerId().getUserId()).setUserName(userContext.getName()).setMessage(message);
        synchronized (chatMessages) {
            chatMessages.add(chatMessage);
            if (chatMessages.size() > CACHE_SIZE) {
                chatMessages.remove(0);
            }
        }
        clientSystemConnectionService.sendChatMessage(chatMessage);
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setTimestamp(new Date());
        chatMessageEntity.setMessage(message);
        chatMessageEntity.setUserEntity(userService.getUserEntity(userContext.getHumanPlayerId().getUserId()));
        chatMessageEntity.setSessionId(playerSession.getHttpSessionId());
        entityManager.persist(chatMessageEntity);
    }

    public void sendLastMessages(PlayerSession session) {
        synchronized (chatMessages) {
            chatMessages.forEach(chatMessage -> clientSystemConnectionService.sendChatMessage(session, chatMessage));
        }
    }
}
