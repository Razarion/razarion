package com.btxtech.server.gameengine;

import com.btxtech.server.AuthHandshakeInterceptor;
import com.btxtech.server.service.engine.ServerLevelQuestService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.ChatMessage;
import com.btxtech.shared.datatypes.LevelUpPacket;
import com.btxtech.shared.datatypes.LifecyclePacket;
import com.btxtech.shared.datatypes.UnlockedItemPacket;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.SlaveQuestInfo;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.system.ConnectionMarshaller;
import com.btxtech.shared.system.SystemConnectionPacket;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.btxtech.server.gameengine.ClientGameConnection.MAPPER;

@Service
public class ClientSystemConnectionService extends TextWebSocketHandler {
    private static final String CLIENT_SYSTEM_CONNECTION = "ClientSystemConnection";
    private final Logger logger = LoggerFactory.getLogger(ClientSystemConnectionService.class);
    private final ObjectMapper mapper = new ObjectMapper();
    /**
     * Every open system connection of a user, oldest first. A list, not one per user: a second tab of
     * the same player used to replace the first, which from then on got nothing from the server -
     * no base, no builder, no tick - while its orders still arrived and were carried out (local
     * test, 2026-10-11). Each of the user's tabs is served now; sends to the user go to all of them.
     */
    private final Map<String, List<ClientSystemConnection>> systemConnections = new TreeMap<>();
    private final Provider<ClientSystemConnection> provider;
    private final Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter;
    private final UserService userService;
    // ObjectProvider breaks the cycle: the announcement service sends through this service.
    private final ObjectProvider<ServerRestartAnnouncementService> serverRestartAnnouncementService;
    private final ObjectProvider<ServerLevelQuestService> serverLevelQuestService;

    public ClientSystemConnectionService(Provider<ClientSystemConnection> provider,
                                         Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter,
                                         UserService userService,
                                         ObjectProvider<ServerRestartAnnouncementService> serverRestartAnnouncementService,
                                         ObjectProvider<ServerLevelQuestService> serverLevelQuestService) {
        this.provider = provider;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.userService = userService;
        this.serverRestartAnnouncementService = serverRestartAnnouncementService;
        this.serverLevelQuestService = serverLevelQuestService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession wsSession) {
        var clientSystemConnection = provider.get();

        Authentication auth = WebSocketUtils.getJwtFromWsSession(wsSession, jwtAuthenticationConverter);
        var httpSessionId = (String) wsSession.getAttributes().get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME);
        var anonymousToken = (String) wsSession.getAttributes().get(AuthHandshakeInterceptor.ANONYMOUS_TOKEN);
        String userId = userService.getOrCreateUserId(auth, httpSessionId, anonymousToken);

        clientSystemConnection.init(wsSession, userId);
        wsSession.getAttributes().put(CLIENT_SYSTEM_CONNECTION, clientSystemConnection);

        synchronized (systemConnections) {
            systemConnections.computeIfAbsent(userId, id -> new ArrayList<>()).add(clientSystemConnection);
        }

        userService.onClientSystemConnectionOpened(userId);

        // A player joining while a restart countdown is running must see it too.
        try {
            serverRestartAnnouncementService.getObject().onSystemConnectionOpened(userId);
        } catch (Throwable throwable) {
            logger.warn(throwable.getMessage(), throwable);
        }

        sendQuestState(userId, clientSystemConnection);

        // TODO connectionTrackingPersistence.onSystemConnectionOpened(clientSystemConnection.getSession().getHttpSessionId(), clientSystemConnection.getSession());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        var clientSystemConnection = (ClientSystemConnection) session.getAttributes().get(CLIENT_SYSTEM_CONNECTION);
        clientSystemConnection.handleMessage(message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        WebSocketUtils.logTransportError(logger, session, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession wsSession, CloseStatus closeStatus) {
        var clientSystemConnection = (ClientSystemConnection) wsSession.getAttributes().get(CLIENT_SYSTEM_CONNECTION);
        boolean last;
        synchronized (systemConnections) {
            // Only this connection, not whatever else the user holds. A reload or a reconnect opens
            // the new connection first and the server may learn of the old one's end only after
            // that - an unclean drop surfaces late. Removing by user id then dropped the NEW one:
            // the player's only tab got nothing from the server any more (2026-10-11, reproduced
            // locally with two tabs).
            last = removeConnection(systemConnections, clientSystemConnection.getUserId(), clientSystemConnection);
            logger.info("Websocket clientSystemConnection closed {}{}", clientSystemConnection,
                    last ? "" : " (the user still holds another connection)");
        }
        if (last) {
            // Not before the last one: sixty minutes after the close stamp the cleanup deletes an
            // anonymous player - who would still be playing on his other connection.
            userService.onClientSystemConnectionClosed(clientSystemConnection.getUserId());
        }
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

    /**
     * The quest the player is on, and how far, to the connection that just opened. Quest changes
     * travel as events on this connection only, so one that came while the connection was down -
     * a mobile drop, which since 2026-10-18 no longer reloads the page - was simply missing: the
     * quest line stayed on the old quest and its tip never came (phone, 2026-10-18). The client
     * ignores an activation of the quest it already shows (GameUiControl#onQuestActivatedServer),
     * so a first connection, which got the same quest in its game context, is not disturbed.
     */
    private void sendQuestState(String userId, ClientSystemConnection clientSystemConnection) {
        try {
            ServerLevelQuestService questService = serverLevelQuestService.getIfAvailable();
            if (questService == null) {
                return;
            }
            SlaveQuestInfo slaveQuestInfo = questService.getSlaveQuestInfo(userId);
            if (slaveQuestInfo.getActiveQuest() == null) {
                return;
            }
            sendToClient(SystemConnectionPacket.QUEST_ACTIVATED, slaveQuestInfo.getActiveQuest(), List.of(clientSystemConnection));
            if (slaveQuestInfo.getQuestProgressInfo() != null) {
                sendToClient(SystemConnectionPacket.QUEST_PROGRESS_CHANGED, slaveQuestInfo.getQuestProgressInfo(), List.of(clientSystemConnection));
            }
        } catch (Throwable throwable) {
            logger.warn("Sending the quest state to a new system connection failed for user " + userId, throwable);
        }
    }

    public void sendLifecyclePacket(LifecyclePacket lifecyclePacket) {
        sendToClients(SystemConnectionPacket.LIFECYCLE_CONTROL, lifecyclePacket);
    }

    public void sendLifecyclePacket(String userId, LifecyclePacket lifecyclePacket) {
        sendToClient(userId, SystemConnectionPacket.LIFECYCLE_CONTROL, lifecyclePacket);
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
            systemConnections.values().forEach(connections -> connections.forEach(connection -> {
                try {
                    connection.sendToClient(text);
                } catch (Throwable throwable) {
                    logger.warn(throwable.getMessage(), throwable);
                }
            }));
        }
    }

    private void sendToClient(String userId, SystemConnectionPacket packet, Object object) {
        List<ClientSystemConnection> connections;
        synchronized (systemConnections) {
            List<ClientSystemConnection> open = systemConnections.get(userId);
            if (open == null) {
                return;
            }
            connections = new ArrayList<>(open);
        }
        try {
            String text;
            if (packet.getTheClass() == Void.class) {
                text = ConnectionMarshaller.marshall(packet, null);
            } else {
                text = ConnectionMarshaller.marshall(packet, MAPPER.writeValueAsString(object));
            }
            for (ClientSystemConnection clientSystemConnection : connections) {
                try {
                    clientSystemConnection.sendToClient(text);
                } catch (Throwable throwable) {
                    logger.warn(throwable.getMessage(), throwable);
                }
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

    /** The user's newest connection, or null when he has none. */
    public ClientSystemConnection getClientSystemConnection(String userId) {
        synchronized (systemConnections) {
            return newest(systemConnections.get(userId));
        }
    }

    /**
     * A copy, taken under the same lock the senders use. Callers get a stable list to work with
     * instead of a view that a closing connection could pull out from under them.
     */
    public Map<String, ClientSystemConnection> getOpenConnections() {
        synchronized (systemConnections) {
            Map<String, ClientSystemConnection> open = new TreeMap<>();
            systemConnections.forEach((userId, connections) -> open.put(userId, newest(connections)));
            return open;
        }
    }

    /**
     * Takes this connection out of the user's list; true when it was his last one. Callers hold the
     * map's lock.
     */
    static <C> boolean removeConnection(Map<String, List<C>> connectionsPerUser, String userId, C connection) {
        List<C> connections = connectionsPerUser.get(userId);
        if (connections == null || !connections.remove(connection)) {
            return false;
        }
        if (connections.isEmpty()) {
            connectionsPerUser.remove(userId);
            return true;
        }
        return false;
    }

    static <C> C newest(List<C> connections) {
        return connections == null || connections.isEmpty() ? null : connections.get(connections.size() - 1);
    }

}
