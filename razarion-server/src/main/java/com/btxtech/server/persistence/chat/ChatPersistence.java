package com.btxtech.server.persistence.chat;

import com.btxtech.server.connection.ClientSystemConnectionService;
import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.shared.datatypes.UserContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Date;

/**
 * Created by Beat
 * on 29.12.2017.
 */
@Singleton
public class ChatPersistence {
    @Inject
    private UserService userService;
    @Inject
    private ClientSystemConnectionService clientSystemConnectionService;
    @PersistenceContext
    private EntityManager entityManager;

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
        clientSystemConnectionService.sendChatMessage(chatMessage);
        ChatMessageEntity chatMessageEntity = new ChatMessageEntity();
        chatMessageEntity.setTimestamp(new Date());
        chatMessageEntity.setMessage(message);
        chatMessageEntity.setUserEntity(userService.getUserEntity(userContext.getHumanPlayerId().getUserId()));
        chatMessageEntity.setSessionId(playerSession.getHttpSessionId());
        entityManager.persist(chatMessageEntity);
    }
}
