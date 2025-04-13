package com.btxtech.server.gameengine;

import com.btxtech.server.user.PlayerSession;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.datatypes.*;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.gameengine.planet.connection.GameConnectionPacket;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final SessionService sessionService;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, ClientSystemConnection> systemConnections = new TreeMap<>();
    private final Provider<ClientSystemConnection> provider;

    public ClientSystemConnectionService(SessionService sessionService,
                                         Provider<ClientSystemConnection> provider) {
        this.sessionService = sessionService;
        this.provider = provider;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession wsSession) {
        var clientSystemConnection = provider.get();

        var httpSessionId = (String) wsSession.getAttributes().get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME);
        clientSystemConnection.init(wsSession, httpSessionId);
        wsSession.getAttributes().put(CLIENT_SYSTEM_CONNECTION, clientSystemConnection);
        synchronized (systemConnections) {
            systemConnections.put(httpSessionId, clientSystemConnection);
        }
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
        var httpSessionId = (String) wsSession.getAttributes().get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME);
        synchronized (systemConnections) {
            var clientSystemConnection = systemConnections.remove(httpSessionId);
            logger.info("Websocket clientSystemConnection closed {}", clientSystemConnection);
        }
    }

    public void onQuestProgressInfo(int userId, QuestProgressInfo questProgressInfo) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.QUEST_PROGRESS_CHANGED, questProgressInfo);
        }
    }

    public void onQuestActivated(int userId, QuestConfig quest) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.QUEST_ACTIVATED, quest);
        }
    }

    public void onQuestPassed(int userId, QuestConfig quest) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.QUEST_PASSED, quest);
        }
    }

    public void onXpChanged(int userId, int xp) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.XP_CHANGED, xp);
        }
    }

    public void onLevelUp(int userId, UserContext newLevelId, boolean availableUnlocks) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.LEVEL_UPDATE_SERVER, new LevelUpPacket().userContext(newLevelId).availableUnlocks(availableUnlocks));
        }
    }

    public void onBoxPicked(int userId, BoxContent boxContent) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.BOX_PICKED, boxContent);
        }
    }

    public void onUnlockedItemLimit(int userId, Map<Integer, Integer> unlockedItemLimit, boolean blinking) {
        PlayerSession playerSession = sessionService.findPlayerSession(userId);
        if (playerSession != null) {
            sendToClient(playerSession, SystemConnectionPacket.UNLOCKED_ITEM_LIMIT, new UnlockedItemPacket().unlockedItemLimit(unlockedItemLimit).availableUnlocks(blinking));
        }
    }

    public void sendLifecyclePacket(LifecyclePacket lifecyclePacket) {
        sendToClients(SystemConnectionPacket.LIFECYCLE_CONTROL, lifecyclePacket);
    }

    public void sendChatMessage(ChatMessage chatMessage) {
        sendToClients(SystemConnectionPacket.CHAT_RECEIVE_MESSAGE, chatMessage);
    }

    public void sendChatMessage(PlayerSession playerSession, ChatMessage chatMessage) {
        sendToClient(playerSession, SystemConnectionPacket.CHAT_RECEIVE_MESSAGE, chatMessage);
    }

    public void sendEmailVerifiedToClient(PlayerSession playerSession) {
        sendToClient(playerSession, SystemConnectionPacket.EMAIL_VERIFIED, null);
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

    private void sendToClient(PlayerSession playerSession, SystemConnectionPacket packet, Object object) {
        ClientSystemConnection clientSystemConnection;
        synchronized (systemConnections) {
            clientSystemConnection = systemConnections.get(playerSession.getHttpSessionId());
            if (clientSystemConnection == null) {
                return;
            }
        }
        try {
            String text = ConnectionMarshaller.marshall(packet, MAPPER.writeValueAsString(object));
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

    // ------------------------------------------

}
