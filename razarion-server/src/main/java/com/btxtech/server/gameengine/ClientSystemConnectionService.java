package com.btxtech.server.gameengine;

import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.shared.datatypes.LevelUpPacket;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.datatypes.UnlockedItemPacket;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import static com.btxtech.server.gameengine.ClientGameConnection.MAPPER;

@Service
public class ClientSystemConnectionService extends TextWebSocketHandler {
    private static final String CLIENT_SYSTEM_CONNECTION = "ClientSystemConnection";
    private final Logger logger = LoggerFactory.getLogger(ClientSystemConnectionService.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, ClientSystemConnection> systemConnections = new TreeMap<>();
    private final Provider<ClientSystemConnection> provider;
    private final Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter;
    private final UserService userService;

    public ClientSystemConnectionService(Provider<ClientSystemConnection> provider,
                                         Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter,
                                         UserService userService) {
        this.provider = provider;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.userService = userService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession wsSession) {
        var clientSystemConnection = provider.get();

        Authentication auth = WebSocketUtils.getJwtFromWsSession(wsSession, jwtAuthenticationConverter);
        var httpSessionId = (String) wsSession.getAttributes().get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME);
        String userId = userService.getOrCreateUserId(auth, httpSessionId);

        clientSystemConnection.init(wsSession, userId);
        wsSession.getAttributes().put(CLIENT_SYSTEM_CONNECTION, clientSystemConnection);

        synchronized (systemConnections) {
            systemConnections.put(userId, clientSystemConnection);
        }

        userService.onClientSystemConnectionOpened(userId);

        // TODO connectionTrackingPersistence.onSystemConnectionOpened(clientSystemConnection.getSession().getHttpSessionId(), clientSystemConnection.getSession());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        var clientSystemConnection = (ClientSystemConnection) session.getAttributes().get(CLIENT_SYSTEM_CONNECTION);
        clientSystemConnection.handleMessage(message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.warn("handleTransportError. Session: {}", session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession wsSession, CloseStatus closeStatus) {
        var clientSystemConnection = (ClientSystemConnection) wsSession.getAttributes().get(CLIENT_SYSTEM_CONNECTION);
        synchronized (systemConnections) {
            systemConnections.remove(clientSystemConnection.getUserId());
            logger.info("Websocket clientSystemConnection closed {}", clientSystemConnection);
        }

        userService.onClientSystemConnectionClosed(clientSystemConnection.getUserId());
    }

    public void onQuestProgressInfo(String userId, QuestProgressInfo questProgressInfo) {
        sendToClient(userId, SystemConnectionPacket.QUEST_PROGRESS_CHANGED, questProgressInfo);
    }

    public void onQuestActivated(String userId, QuestConfig quest) {
        sendToClient(userId, SystemConnectionPacket.QUEST_ACTIVATED, quest);
    }

    public void onQuestPassed(String userId, QuestConfig quest) {
        sendToClient(userId, SystemConnectionPacket.QUEST_PASSED, quest);
    }

    public void onAllQuestsCompleted(String userId) {
        sendToClient(userId, SystemConnectionPacket.ALL_QUESTS_COMPLETED, null);
    }

    public void onXpChanged(String userId, int xp) {
        sendToClient(userId, SystemConnectionPacket.XP_CHANGED, xp);
    }

    public void onLevelUp(String userId, UserContext newLevelId, boolean availableUnlocks) {
        sendToClient(userId, SystemConnectionPacket.LEVEL_UPDATE_SERVER, new LevelUpPacket().userContext(newLevelId).availableUnlocks(availableUnlocks));
    }

    public void onBoxPicked(String userId, BoxContent boxContent) {
        sendToClient(userId, SystemConnectionPacket.BOX_PICKED, boxContent);
    }

    public void onUnlockedItemLimit(String userId, Map<Integer, Integer> unlockedItemLimit, boolean blinking) {
        sendToClient(userId, SystemConnectionPacket.UNLOCKED_ITEM_LIMIT, new UnlockedItemPacket().unlockedItemLimit(unlockedItemLimit).availableUnlocks(blinking));
    }

    public void sendLifecyclePacket(LifecyclePacket lifecyclePacket) {
        sendToClients(SystemConnectionPacket.LIFECYCLE_CONTROL, lifecyclePacket);
    }

    public void sendChatMessage(ChatMessage chatMessage) {
        sendToClients(SystemConnectionPacket.CHAT_RECEIVE_MESSAGE, chatMessage);
    }

    private void sendToClients(SystemConnectionPacket packet, Object object) {
        String text;
        try {
            text = ConnectionMarshaller.marshall(packet, MAPPER.writeValueAsString(object));
        } catch (Throwable throwable) {
            logger.warn(throwable.getMessage(), throwable);
            return;
        }

        synchronized (systemConnections) {
            systemConnections.values().forEach(gameConnection -> {
                try {
                    gameConnection.sendToClient(text);
                } catch (Throwable throwable) {
                    logger.warn(throwable.getMessage(), throwable);
                }
            });
        }
    }

    private void sendToClient(String userId, SystemConnectionPacket packet, Object object) {
        ClientSystemConnection clientSystemConnection;
        synchronized (systemConnections) {
            clientSystemConnection = systemConnections.get(userId);
            if (clientSystemConnection == null) {
                return;
            }
        }
        try {
            String text;
            if (packet.getTheClass() == Void.class) {
                text = ConnectionMarshaller.marshall(packet, null);
            } else {
                text = ConnectionMarshaller.marshall(packet, MAPPER.writeValueAsString(object));
            }
            try {
                clientSystemConnection.sendToClient(text);
            } catch (Throwable throwable) {
                logger.warn(throwable.getMessage(), throwable);
            }
        } catch (Throwable throwable) {
            logger.warn(throwable.getMessage(), throwable);
        }
    }

    private void sendToClient(SystemConnectionPacket packet, Object object, Collection<ClientSystemConnection> clientSystemConnections) {
        try {
            String text;
            if (packet.getTheClass() == Void.class) {
                text = ConnectionMarshaller.marshall(packet, null);
            } else {
                text = ConnectionMarshaller.marshall(packet, mapper.writeValueAsString(object));
            }
            clientSystemConnections.forEach(clientSystemConnection -> {
                try {
                    clientSystemConnection.sendToClient(text);
                } catch (Throwable throwable) {
                    logger.warn(throwable.getMessage(), throwable);
                }
            });
        } catch (Throwable throwable) {
            logger.warn(throwable.getMessage(), throwable);
        }
    }

    public ClientSystemConnection getClientSystemConnection(String userId) {
        return systemConnections.get(userId);
    }

}
